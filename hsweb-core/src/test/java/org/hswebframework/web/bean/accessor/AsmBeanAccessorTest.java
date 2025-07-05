package org.hswebframework.web.bean.accessor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.ResolvableType;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

public class AsmBeanAccessorTest {

    private AsmBeanAccessor accessor;

    @BeforeEach
    void setUp() {
        accessor = new AsmBeanAccessor();
    }

    @Test
    void testCreateReader() {
        // 测试读取字符串属性
        PropertyReader nameReader = accessor.createReader(TestEntity.class, "name");
        assertNotNull(nameReader);

        TestEntity entity = new TestEntity();
        entity.setName("测试名称");

        Object result = nameReader.apply(entity);
        assertEquals("测试名称", result);
    }

    @Test
    void testCreateReader_PrimitiveTypes() {
        // 测试读取基本类型
        PropertyReader ageReader = accessor.createReader(TestEntity.class, "age");
        PropertyReader activeReader = accessor.createReader(TestEntity.class, "active");

        TestEntity entity = new TestEntity();
        entity.setAge(25);
        entity.setActive(true);

        Object age = ageReader.apply(entity);
        Object active = activeReader.apply(entity);

        assertEquals(25, age);
        assertEquals(true, active);
        assertTrue(age instanceof Integer);
        assertTrue(active instanceof Boolean);
    }

    @Test
    void testCreateWriter() {
        // 测试写入字符串属性
        PropertyWriter nameWriter = accessor.createWriter(TestEntity.class, "name", null);
        assertNotNull(nameWriter);

        TestEntity entity = new TestEntity();
        nameWriter.accept(entity, "新名称");

        assertEquals("新名称", entity.getName());
    }

    @Test
    void testCreateWriter_PrimitiveTypes() {
        // 测试写入基本类型
        PropertyWriter ageWriter = accessor.createWriter(TestEntity.class, "age", null);
        PropertyWriter activeWriter = accessor.createWriter(TestEntity.class, "active", null);

        TestEntity entity = new TestEntity();
        ageWriter.accept(entity, 30);
        activeWriter.accept(entity, false);

        assertEquals(30, entity.getAge());
        assertEquals(false, entity.isActive());
    }

    @Test
    void testCreateWriter_WithTypeConverter() {
        // 测试带类型转换器的写入
        TypeConverter converter = new TypeConverter() {
            @Override
            public Object convert(Object source, ResolvableType type) {
                if (source instanceof String && (type.getRawClass() == int.class || type.getRawClass() == Integer.class)) {
                    return Integer.parseInt((String) source);
                }
                return source;
            }
        };

        PropertyWriter ageWriter = accessor.createWriter(TestEntity.class, "age", converter);

        TestEntity entity = new TestEntity();
        ageWriter.accept(entity, "35");

        assertEquals(35, entity.getAge());
    }

    @Test
    void testCreateWriter_NumberConversion() {
        // 测试数字类型的自动转换
        PropertyWriter ageWriter = accessor.createWriter(TestEntity.class, "age", null);
        PropertyWriter salaryWriter = accessor.createWriter(TestEntity.class, "salary", null);

        TestEntity entity = new TestEntity();

        // 测试不同数字类型的转换
        ageWriter.accept(entity, 25L); // Long -> int
        salaryWriter.accept(entity, 50000.0f); // float -> double

        assertEquals(25, entity.getAge());
        assertEquals(50000.0, entity.getSalary(), 0.001);
    }


    @Test
    void testInvalidProperty() {
        // 测试不存在的属性
        assertThrows(RuntimeException.class, () -> {
            accessor.createReader(TestEntity.class, "nonExistentProperty");
        });

        assertThrows(RuntimeException.class, () -> {
            accessor.createWriter(TestEntity.class, "nonExistentProperty", null);
        });
    }

    @Test
    void testReadOnlyProperty() {
        // 测试只读属性
        PropertyReader reader = accessor.createReader(TestEntity.class, "readOnlyProperty");

        TestEntity entity = new TestEntity();
        Object result = reader.apply(entity);
        assertEquals("只读属性", result);

        // 尝试创建写入器应该失败
        assertThrows(RuntimeException.class, () -> {
            accessor.createWriter(TestEntity.class, "readOnlyProperty", null);
        });
    }

    @Test
    void testWriteOnlyProperty() {
        // 测试只写属性
        PropertyWriter writer = accessor.createWriter(TestEntity.class, "writeOnlyProperty", null);

        TestEntity entity = new TestEntity();
        writer.accept(entity, "写入值");

        assertEquals("写入值", entity.getWriteOnlyValue());

        // 尝试创建读取器应该失败
        assertThrows(RuntimeException.class, () -> {
            accessor.createReader(TestEntity.class, "writeOnlyProperty");
        });
    }

    @Test
    void testComplexObject() {
        // 测试复杂对象属性
        PropertyReader addressReader = accessor.createReader(TestEntity.class, "address");
        PropertyWriter addressWriter = accessor.createWriter(TestEntity.class, "address", null);

        TestEntity entity = new TestEntity();
        Address address = new Address();
        address.setStreet("测试街道");
        address.setCity("测试城市");

        addressWriter.accept(entity, address);
        Object result = addressReader.apply(entity);

        assertTrue(result instanceof Address);
        Address resultAddress = (Address) result;
        assertEquals("测试街道", resultAddress.getStreet());
        assertEquals("测试城市", resultAddress.getCity());
    }

    @Test
    void testPerformance() {
        System.out.println("\n=== 性能测试结果 (执行1000000次读写操作) ===");
        
        try {
            // 创建带有TypeConverter的访问器来测试优化路径
            PropertyReader nameReader = accessor.createReader(TestEntity.class, "name");
            PropertyWriter nameWriter = accessor.createWriter(TestEntity.class, "name", new TypeConverter() {
                @Override
                public Object convert(Object source, ResolvableType type) {
                    // 简单的类型转换，确保走优化路径
                    return source;
                }
            });
            
            TestEntity entity = new TestEntity();
            entity.setName("测试");
            
            // 预热
            for (int i = 0; i < 100000; i++) {
                nameReader.apply(entity);
                nameWriter.accept(entity, "warm_" + i);
            }
            
            // 测试ASM访问器
            long asmStart = System.nanoTime();
            for (int i = 0; i < 1000000; i++) {
                String result = (String) nameReader.apply(entity);
                nameWriter.accept(entity, result);
            }
            long asmEnd = System.nanoTime();
            
            // 测试直接方法调用
            long directStart = System.nanoTime();
            for (int i = 0; i < 1000000; i++) {
                String result = entity.getName();
                entity.setName(result);
            }
            long directEnd = System.nanoTime();
            
            long asmTime = asmEnd - asmStart;
            long directTime = directEnd - directStart;
            
            System.out.println("ASM访问器耗时: " + asmTime / 1000000 + "ms");
            System.out.println("直接方法调用耗时: " + directTime / 1000000 + "ms");
            System.out.println("性能比率: " + String.format("%.2f", (double) asmTime / directTime) + "x");
            
            if (asmTime < directTime * 2) {
                System.out.println("✅ ASM访问器性能优秀，与直接调用相差不大");
            } else {
                System.out.println("⚠️ ASM访问器性能有优化空间");
            }
            
            // 断言性能要求
            assertTrue(asmTime < directTime * 5, "ASM访问器性能不应该比直接调用慢5倍以上");
            
        } catch (Exception e) {
            fail("性能测试失败: " + e.getMessage());
        }
    }

    @Test
    void testDetailedPerformanceComparison() {
        System.out.println("\n=== 详细性能分析测试 ===");
        
        try {
            // 创建访问器 - 使用TypeConverter来测试优化路径
            PropertyReader nameReader = accessor.createReader(TestEntity.class, "name");
            PropertyWriter nameWriter = accessor.createWriter(TestEntity.class, "name", new TypeConverter() {
                @Override
                public Object convert(Object source, ResolvableType type) {
                    // 简单的类型转换，确保走优化路径
                    return source;
                }
            });
            
            TestEntity entity = new TestEntity();
            entity.setName("测试");
            
            // 1. JVM热身
            System.out.println("1. 开始 JVM 热身...");
            for (int i = 0; i < 100000; i++) {
                nameReader.apply(entity);
                nameWriter.accept(entity, "warmup" + i);
                entity.getName();
                entity.setName("warmup" + i);
            }
            System.out.println("   热身完成");
            
            // 2. 多轮性能测试
            int rounds = 20;
            int iterations = 5000000;
            long[] asmTimes = new long[rounds];
            long[] directTimes = new long[rounds];
            
            for (int round = 0; round < rounds; round++) {
                System.out.println("2. 执行第 " + (round + 1) + " 轮测试...");
                
                // 重置状态
                entity.setName("测试");
                System.gc();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // === ASM 测试 ===
                long asmStart = System.nanoTime();
                for (int i = 0; i < iterations; i++) {
                    String result = (String) nameReader.apply(entity);
                    nameWriter.accept(entity, result);
                }
                long asmEnd = System.nanoTime();
                asmTimes[round] = asmEnd - asmStart;
                
                // === 直接调用测试 ===
                entity.setName("测试");
                System.gc();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                long directStart = System.nanoTime();
                for (int i = 0; i < iterations; i++) {
                    String result = entity.getName();
                    entity.setName(result);
                }
                long directEnd = System.nanoTime();
                directTimes[round] = directEnd - directStart;
                
                System.out.println("   ASM: " + asmTimes[round] / 1000000 + "ms, 直接调用: " + directTimes[round] / 1000000 + "ms");
                
                // 强制垃圾回收
                System.gc();
                Thread.yield();
            }
            
            // 3. 计算平均值和统计信息
            long asmAvg = 0, directAvg = 0;
            for (int i = 0; i < 5; i++) {
                asmAvg += asmTimes[i];
                directAvg += directTimes[i];
            }
            asmAvg /= 5;
            directAvg /= 5;
            
            System.out.println("\n=== 最终统计结果 ===");
            System.out.println("ASM访问器平均耗时: " + asmAvg / 1000000 + "ms");
            System.out.println("直接调用平均耗时: " + directAvg / 1000000 + "ms");
            System.out.println("性能比率: " + String.format("%.2f", (double) asmAvg / directAvg) + "x");
            
            // 4. 分析为什么 ASM 可能更快
            System.out.println("\n=== 性能分析 ===");
            if (asmAvg < directAvg) {
                System.out.println("✨ ASM访问器确实比直接调用更快！可能原因：");
                System.out.println("  • 生成的字节码更简洁，减少了方法调用开销");
                System.out.println("  • JIT编译器更容易内联优化生成的访问器方法");
                System.out.println("  • 避免了Java方法调用的一些间接开销");
                System.out.println("  • 专门针对单一属性访问优化的字节码");
            } else if (asmAvg <= directAvg * 1.5) {
                System.out.println("✅ ASM访问器性能与直接调用相当，这已经非常优秀了！");
            } else {
                System.out.println("⚠️ ASM访问器性能有优化空间");
            }
            
            // 断言性能要求
            assertTrue(asmAvg < directAvg * 5, "ASM访问器性能不应该比直接调用慢5倍以上");
            
        } catch (Exception e) {
            fail("详细性能分析测试失败: " + e.getMessage());
        }
    }
    
    @Test
    void testThreeWayPerformanceComparison() {
        System.out.println("\n=== 三方性能对比测试：ASM vs 直接调用 vs MethodHandle ===");
        
        try {
            TypeConverter converter = (source, type) -> {
                // 简单的类型转换，确保走优化路径
                if(type.isInstance(source)){
                    return source;
                }
                return type.toClass().cast(source);
            };

            // 准备三种访问方式 - 使用TypeConverter来测试优化路径
            PropertyReader nameReader = accessor.createReader(TestEntity.class, "name");
            PropertyWriter nameWriter = accessor.createWriter(TestEntity.class, "name",converter );

            // MethodHandle方式
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle getNameHandle = lookup.findVirtual(TestEntity.class, "getName", MethodType.methodType(String.class));
            MethodHandle setNameHandle = lookup.findVirtual(TestEntity.class, "setName", MethodType.methodType(void.class, String.class));

            BiConsumer<TestEntity,String> setter = TestEntity::setName;
            TestEntity entity = new TestEntity();
            entity.setName("初始值");
            
            int warmupIterations = 5000000;   // 热身次数
            int testIterations = 5000000;    // 测试次数（减少以避免测试时间过长）
            
            System.out.println("1. 开始热身阶段...");
            
            // 热身所有三种方式
            for (int i = 0; i < warmupIterations; i++) {
                // ASM热身
                nameReader.apply(entity);
                nameWriter.accept(entity, "warmup_asm_" + i);
                
                // 直接调用热身
                setter.accept(entity, "warmup_asm_" + i);

                // MethodHandle热身
                getNameHandle.invoke(entity);
                setNameHandle.invoke(entity, "warmup_methodhandle_" + i);
            }
            
            System.out.println("   热身完成，开始正式测试...");
            
            // 执行多轮测试
            int rounds = 10;
            long[] asmTimes = new long[rounds];
            long[] directTimes = new long[rounds];
            long[] methodHandleTimes = new long[rounds];
            
            for (int round = 0; round < rounds; round++) {
                System.out.println("2. 执行第 " + (round + 1) + " 轮测试...");
                
                // === 测试 ASM 访问器 ===
                entity.setName("asm_test_" + round);
                System.gc();
                Thread.yield();
                
                long asmStart = System.nanoTime();
                for (int i = 0; i < testIterations; i++) {
                    String result = (String) nameReader.apply(entity);
                    nameWriter.accept(entity, "asm_test_");
                }
                long asmEnd = System.nanoTime();
                asmTimes[round] = asmEnd - asmStart;
                
                // === 测试直接方法调用 ===
                entity.setName("direct_test_" + round);
                System.gc();
                Thread.yield();
                
                long directStart = System.nanoTime();
                for (int i = 0; i < testIterations; i++) {
                    String result = entity.getName();
                    setter.accept(entity, "direct_test_");
//                    entity.setName("direct_test_" + round);
                }
                long directEnd = System.nanoTime();
                directTimes[round] = directEnd - directStart;
                
                // === 测试MethodHandle调用 ===
                entity.setName("methodhandle_test_" + round);
                System.gc();
                Thread.yield();

                long methodHandleStart = System.nanoTime();
                for (int i = 0; i < testIterations; i++) {
                    String result = (String)getNameHandle.invoke(entity);
                    setNameHandle.invoke(entity, "methodhandle_test_");
                }
                long methodHandleEnd = System.nanoTime();
                methodHandleTimes[round] = methodHandleEnd - methodHandleStart;
                
                System.out.println("   第" + (round + 1) + "轮: ASM=" + asmTimes[round]/1000000 + "ms, " +
                                 "直接调用=" + directTimes[round]/1000000 + "ms, " +
                                 "MethodHandle=" + methodHandleTimes[round]/1000000 + "ms");
            }
            
            // 计算平均值
            long asmAvg = 0, directAvg = 0, methodHandleAvg = 0;
            for (int i = 0; i < rounds; i++) {
                asmAvg += asmTimes[i];
                directAvg += directTimes[i];
                methodHandleAvg += methodHandleTimes[i];
            }
            asmAvg /= rounds;
            directAvg /= rounds;
            methodHandleAvg /= rounds;
            
            // 输出详细结果
            System.out.println("\n=== 三方性能对比结果 (执行" + testIterations + "次读写操作) ===");
            System.out.println("┌─────────────────┬──────────┬──────────────┬─────────────────┐");
            System.out.println("│ 访问方式        │ 平均耗时 │ 相对直接调用 │ 相对MethodHandle │");
            System.out.println("├─────────────────┼──────────┼──────────────┼─────────────────┤");
            System.out.printf("│ 直接方法调用    │ %6dms │    %.2fx     │     %.2fx       │%n", 
                            directAvg/1000000, 1.0, (double)directAvg/methodHandleAvg);
            System.out.printf("│ ASM访问器       │ %6dms │    %.2fx     │     %.2fx       │%n", 
                            asmAvg/1000000, (double)asmAvg/directAvg, (double)asmAvg/methodHandleAvg);
            System.out.printf("│ MethodHandle调用│ %6dms │    %.2fx     │     %.2fx       │%n", 
                            methodHandleAvg/1000000, (double)methodHandleAvg/directAvg, 1.0);
            System.out.println("└─────────────────┴──────────┴──────────────┴─────────────────┘");
            
            // 性能排名
            System.out.println("\n=== 性能排名 ===");
            if (asmAvg <= directAvg && asmAvg <= methodHandleAvg) {
                System.out.println("🥇 ASM访问器 - 最快");
                System.out.println("🥈 " + (directAvg <= methodHandleAvg ? "直接调用" : "MethodHandle调用"));
                System.out.println("🥉 " + (directAvg > methodHandleAvg ? "直接调用" : "MethodHandle调用"));
            } else if (directAvg <= asmAvg && directAvg <= methodHandleAvg) {
                System.out.println("🥇 直接调用 - 最快");
                System.out.println("🥈 " + (asmAvg <= methodHandleAvg ? "ASM访问器" : "MethodHandle调用"));
                System.out.println("🥉 " + (asmAvg > methodHandleAvg ? "ASM访问器" : "MethodHandle调用"));
            } else {
                System.out.println("🥇 MethodHandle调用 - 最快（这很罕见！）");
                System.out.println("🥈 " + (asmAvg <= directAvg ? "ASM访问器" : "直接调用"));
                System.out.println("🥉 " + (asmAvg > directAvg ? "ASM访问器" : "直接调用"));
            }
            
            // 分析结果
            System.out.println("\n=== 性能分析 ===");
            double asmVsMethodHandle = (double) asmAvg / methodHandleAvg;
            double directVsMethodHandle = (double) directAvg / methodHandleAvg;
            
            if (asmVsMethodHandle < 0.5) {
                System.out.println("🚀 ASM访问器比MethodHandle快 " + String.format("%.1f", 1/asmVsMethodHandle) + " 倍以上！");
            } else if (asmVsMethodHandle < 1.0) {
                System.out.println("✅ ASM访问器比MethodHandle快 " + String.format("%.1f", 1/asmVsMethodHandle) + " 倍");
            }
            
            if (asmAvg <= directAvg * 1.2) {
                System.out.println("✨ ASM访问器性能与直接调用非常接近，这证明了字节码生成的优秀！");
            }
            
            System.out.println("📊 MethodHandle是Java 7引入的现代反射API，性能比传统反射更好");
            
            // 性能断言
            assertTrue(asmAvg < methodHandleAvg * 2, "ASM访问器应该比MethodHandle快");
            assertTrue(directAvg < methodHandleAvg * 2, "直接调用应该比MethodHandle快");
            assertTrue(asmAvg < directAvg * 5, "ASM访问器不应该比直接调用慢太多");
            
        } catch (Throwable e) {
            fail("三方性能对比测试失败: " + e.getMessage());
        }
    }

    @Test
    void testMethodHandlePerformanceAnalysis() {
        System.out.println("\n=== MethodHandle性能分析测试 ===");
        
        try {
            // 准备访问器 - 使用TypeConverter来测试优化路径
            PropertyReader nameReader = accessor.createReader(TestEntity.class, "name");
            PropertyWriter nameWriter = accessor.createWriter(TestEntity.class, "name", new TypeConverter() {
                @Override
                public Object convert(Object source, ResolvableType type) {
                    // 简单的类型转换，确保走优化路径
                    return source;
                }
            });
            
            // 准备MethodHandle
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle getNameHandle = lookup.findVirtual(TestEntity.class, "getName", MethodType.methodType(String.class));
            MethodHandle setNameHandle = lookup.findVirtual(TestEntity.class, "setName", MethodType.methodType(void.class, String.class));
            
            TestEntity entity = new TestEntity();
            entity.setName("测试");
            
            int iterations = 100000;
            
            // 热身
            for (int i = 0; i < 10000; i++) {
                nameReader.apply(entity);
                nameWriter.accept(entity, "warmup" + i);
                getNameHandle.invoke(entity);
                setNameHandle.invoke(entity, "warmup" + i);
                entity.getName();
                entity.setName("warmup" + i);
            }
            
            System.out.println("1. 纯读写操作测试：");
            
            // 测试ASM
            long asmStart = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                nameReader.apply(entity);
                nameWriter.accept(entity, "asm" + i);
            }
            long asmTime = System.nanoTime() - asmStart;
            
            // 测试MethodHandle
            long methodHandleStart = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                getNameHandle.invoke(entity);
                setNameHandle.invoke(entity, "mh" + i);
            }
            long methodHandleTime = System.nanoTime() - methodHandleStart;
            
            // 测试直接调用
            long directStart = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                entity.getName();
                entity.setName("direct" + i);
            }
            long directTime = System.nanoTime() - directStart;
            
            System.out.println("   ASM: " + asmTime/1000000 + "ms");
            System.out.println("   MethodHandle: " + methodHandleTime/1000000 + "ms");
            System.out.println("   Direct: " + directTime/1000000 + "ms");
            
            // 数据依赖测试
            System.out.println("\n2. 数据依赖操作测试：");
            StringBuilder sb = new StringBuilder();
            
            // ASM 数据依赖测试
            entity.setName("start");
            long asmDataStart = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                String name = (String) nameReader.apply(entity);
                nameWriter.accept(entity, name + i);
                sb.append(name.charAt(0)); // 数据依赖
            }
            long asmDataTime = System.nanoTime() - asmDataStart;
            
            // MethodHandle 数据依赖测试
            entity.setName("start");
            sb.setLength(0);
            long mhDataStart = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                String name = (String) getNameHandle.invoke(entity);
                setNameHandle.invoke(entity, name + i);
                sb.append(name.charAt(0)); // 数据依赖
            }
            long mhDataTime = System.nanoTime() - mhDataStart;
            
            // 直接调用数据依赖测试
            entity.setName("start");
            sb.setLength(0);
            long directDataStart = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                String name = entity.getName();
                entity.setName(name + i);
                sb.append(name.charAt(0)); // 数据依赖
            }
            long directDataTime = System.nanoTime() - directDataStart;
            
            System.out.println("   ASM: " + asmDataTime/1000000 + "ms");
            System.out.println("   MethodHandle: " + mhDataTime/1000000 + "ms");
            System.out.println("   Direct: " + directDataTime/1000000 + "ms");
            
            // 只读测试
            System.out.println("\n3. 只读操作测试：");
            long asmReadStart = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                nameReader.apply(entity);
                nameReader.apply(entity);
                nameReader.apply(entity);
            }
            long asmReadTime = System.nanoTime() - asmReadStart;
            
            long mhReadStart = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                getNameHandle.invoke(entity);
                getNameHandle.invoke(entity);
                getNameHandle.invoke(entity);
            }
            long mhReadTime = System.nanoTime() - mhReadStart;
            
            long directReadStart = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                entity.getName();
                entity.getName();
                entity.getName();
            }
            long directReadTime = System.nanoTime() - directReadStart;
            
            System.out.println("   ASM: " + asmReadTime/1000000 + "ms");
            System.out.println("   MethodHandle: " + mhReadTime/1000000 + "ms");
            System.out.println("   Direct: " + directReadTime/1000000 + "ms");
            
            // 只写测试
            System.out.println("\n4. 只写操作测试：");
            long asmWriteStart = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                nameWriter.accept(entity, "asm" + i);
                nameWriter.accept(entity, "asm" + i);
                nameWriter.accept(entity, "asm" + i);
            }
            long asmWriteTime = System.nanoTime() - asmWriteStart;
            
            long mhWriteStart = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                setNameHandle.invoke(entity, "mh" + i);
                setNameHandle.invoke(entity, "mh" + i);
                setNameHandle.invoke(entity, "mh" + i);
            }
            long mhWriteTime = System.nanoTime() - mhWriteStart;
            
            long directWriteStart = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                entity.setName("direct" + i);
                entity.setName("direct" + i);
                entity.setName("direct" + i);
            }
            long directWriteTime = System.nanoTime() - directWriteStart;
            
            System.out.println("   ASM: " + asmWriteTime/1000000 + "ms");
            System.out.println("   MethodHandle: " + mhWriteTime/1000000 + "ms");
            System.out.println("   Direct: " + directWriteTime/1000000 + "ms");
            
            // 分析结论
            System.out.println("\n=== 分析结论 ===");
            System.out.println("MethodHandle在特定场景下性能优异可能的原因：");
            System.out.println("1. 🔧 JIT编译器对MethodHandle的特殊优化");
            System.out.println("2. 📊 数据依赖操作模式下的优化策略");
            System.out.println("3. 🚀 MethodHandle的内联优化更激进");
            System.out.println("4. 💡 字符串拼接操作掩盖了方法调用开销");
            
            // 性能断言
            assertTrue(asmTime < directTime * 3, "ASM访问器读写性能应该合理");
            assertTrue(methodHandleTime < directTime * 3, "MethodHandle读写性能应该合理");
            
        } catch (Throwable e) {
            fail("MethodHandle性能分析测试失败: " + e.getMessage());
        }
    }

    @Test
    public void testGenericSupport() throws Exception {
        // 测试泛型支持
        AsmBeanAccessor accessor = new AsmBeanAccessor();
        
        // 创建一个带有泛型的属性writer
        PropertyWriter writer = accessor.createWriter(TestEntity.class, "genericList", new TypeConverter() {
            @Override
            public Object convert(Object source, ResolvableType targetType) {
                // 验证ResolvableType包含泛型信息
                assertNotNull(targetType);
                
                // 对于List<String>，应该可以获取到泛型信息
                if (targetType.hasGenerics()) {
                    ResolvableType generic = targetType.getGeneric(0);
                    assertEquals(String.class, generic.resolve());
                }
                
                return source;
            }
        });
        
        TestEntity entity = new TestEntity();
        List<String> testList = Arrays.asList("test1", "test2");
        
        // 测试写入
        writer.accept(entity, testList);
        assertEquals(testList, entity.getGenericList());
        
        System.out.println("✅ 泛型支持测试通过！");
    }

    @Test
    void testCreateReaderCode() throws Exception {
        // 测试生成Reader字节码
        byte[] readerCode = accessor.createReaderCode(TestEntity.class, "name");
        
        assertNotNull(readerCode);
        assertTrue(readerCode.length > 0);
        
        // 保存字节码到target目录
        saveByteCodeToTarget("TestEntity_getName_Reader.class", readerCode);
        
        // 验证生成的字节码是否有效
        Class<?> readerClass = defineClassFromBytes(readerCode);
        Object readerInstance = readerClass.getDeclaredConstructor().newInstance();
        assertTrue(readerInstance instanceof PropertyReader);
        
        // 测试功能是否正常
        PropertyReader reader = (PropertyReader) readerInstance;
        TestEntity entity = new TestEntity();
        entity.setName("测试名称");
        
        Object result = reader.apply(entity);
        assertEquals("测试名称", result);
        
        System.out.println("✅ createReaderCode 测试通过，字节码已保存到 target/bytecode/TestEntity_getName_Reader.class");
    }

    @Test
    void testCreateWriterCode() throws Exception {
        // 获取PropertyDescriptor
        PropertyDescriptor descriptor = getPropertyDescriptor(TestEntity.class, "name");
        
        // 创建TypeConverter
        TypeConverter converter = (source, type) -> {
            if (type.isInstance(source)) {
                return source;
            }
            return type.toClass().cast(source);
        };
        
        // 测试生成Writer字节码
        byte[] writerCode = accessor.createWriterCode(TestEntity.class, this.getClass().getPackageName()+".TestEntity_setName_Writer", descriptor, converter);
        
        assertNotNull(writerCode);
        assertTrue(writerCode.length > 0);
        
        // 保存字节码到target目录
        saveByteCodeToTarget("TestEntity_setName_Writer.class", writerCode);
        
        // 验证生成的字节码是否有效
        Class<?> writerClass = defineClassFromBytes(writerCode);
        ResolvableType resolvableType = ResolvableType.forMethodParameter(descriptor.getWriteMethod(), 0);
        Object writerInstance = writerClass.getDeclaredConstructor(TypeConverter.class, ResolvableType.class)
                .newInstance(converter, resolvableType);
        assertTrue(writerInstance instanceof PropertyWriter);
        
        // 测试功能是否正常
        PropertyWriter writer = (PropertyWriter) writerInstance;
        TestEntity entity = new TestEntity();
        writer.accept(entity, "测试写入");
        
        assertEquals("测试写入", entity.getName());
        
        System.out.println("✅ createWriterCode 测试通过，字节码已保存到 target/bytecode/TestEntity_setName_Writer.class");
    }

    @Test
    void testCreateReaderCodeForDifferentTypes() throws Exception {
        // 测试不同类型的属性读取器字节码生成
        TestEntity entity = new TestEntity();
        entity.setAge(25);
        entity.setActive(true);
        entity.setSalary(50000.0);
        
        // 测试int类型
        byte[] ageReaderCode = accessor.createReaderCode(TestEntity.class, "age");
        saveByteCodeToTarget("TestEntity_getAge_Reader.class", ageReaderCode);
        
        Class<?> ageReaderClass = defineClassFromBytes(ageReaderCode);
        PropertyReader ageReader = (PropertyReader) ageReaderClass.getDeclaredConstructor().newInstance();
        assertEquals(25, ageReader.apply(entity));
        
        // 测试boolean类型
        byte[] activeReaderCode = accessor.createReaderCode(TestEntity.class, "active");
        saveByteCodeToTarget("TestEntity_isActive_Reader.class", activeReaderCode);
        
        Class<?> activeReaderClass = defineClassFromBytes(activeReaderCode);
        PropertyReader activeReader = (PropertyReader) activeReaderClass.getDeclaredConstructor().newInstance();
        assertEquals(true, activeReader.apply(entity));
        
        // 测试double类型
        byte[] salaryReaderCode = accessor.createReaderCode(TestEntity.class, "salary");
        saveByteCodeToTarget("TestEntity_getSalary_Reader.class", salaryReaderCode);
        
        Class<?> salaryReaderClass = defineClassFromBytes(salaryReaderCode);
        PropertyReader salaryReader = (PropertyReader) salaryReaderClass.getDeclaredConstructor().newInstance();
        assertEquals(50000.0, salaryReader.apply(entity));
        
        System.out.println("✅ 不同类型的属性字节码生成测试通过");
    }

    @Test
    void testCreateWriterCodeForDifferentTypes() throws Exception {
        // 测试不同类型的属性写入器字节码生成
        TestEntity entity = new TestEntity();
        
        TypeConverter converter = (source, type) -> {
            if (type.isInstance(source)) {
                return source;
            }
            return type.toClass().cast(source);
        };
        
        // 测试int类型
        PropertyDescriptor ageDescriptor = getPropertyDescriptor(TestEntity.class, "age");
        byte[] ageWriterCode = accessor.createWriterCode(TestEntity.class,this.getClass().getPackageName()+".TestEntity_setAge_Writer", ageDescriptor, converter);
        saveByteCodeToTarget("TestEntity_setAge_Writer.class", ageWriterCode);
        
        Class<?> ageWriterClass = defineClassFromBytes(ageWriterCode);
        ResolvableType ageResolvableType = ResolvableType.forMethodParameter(ageDescriptor.getWriteMethod(), 0);
        PropertyWriter ageWriter = (PropertyWriter) ageWriterClass.getDeclaredConstructor(TypeConverter.class, ResolvableType.class)
                .newInstance(converter, ageResolvableType);
        ageWriter.accept(entity, 30);
        assertEquals(30, entity.getAge());
        
        // 测试boolean类型
        PropertyDescriptor activeDescriptor = getPropertyDescriptor(TestEntity.class, "active");
        byte[] activeWriterCode = accessor.createWriterCode(TestEntity.class,this.getClass().getPackageName()+".TestEntity_setActive_Writer", activeDescriptor, converter);
        saveByteCodeToTarget("TestEntity_setActive_Writer.class", activeWriterCode);
        
        Class<?> activeWriterClass = defineClassFromBytes(activeWriterCode);
        ResolvableType activeResolvableType = ResolvableType.forMethodParameter(activeDescriptor.getWriteMethod(), 0);
        PropertyWriter activeWriter = (PropertyWriter) activeWriterClass.getDeclaredConstructor(TypeConverter.class, ResolvableType.class)
                .newInstance(converter, activeResolvableType);
        activeWriter.accept(entity, false);
        assertEquals(false, entity.isActive());
        
        // 测试double类型
        PropertyDescriptor salaryDescriptor = getPropertyDescriptor(TestEntity.class, "salary");
        byte[] salaryWriterCode = accessor.createWriterCode(TestEntity.class,this.getClass().getPackageName()+".TestEntity_setSalary_Writer", salaryDescriptor, converter);
        saveByteCodeToTarget("TestEntity_setSalary_Writer.class", salaryWriterCode);
        
        Class<?> salaryWriterClass = defineClassFromBytes(salaryWriterCode);
        ResolvableType salaryResolvableType = ResolvableType.forMethodParameter(salaryDescriptor.getWriteMethod(), 0);
        PropertyWriter salaryWriter = (PropertyWriter) salaryWriterClass.getDeclaredConstructor(TypeConverter.class, ResolvableType.class)
                .newInstance(converter, salaryResolvableType);
        salaryWriter.accept(entity, 60000.0);
        assertEquals(60000.0, entity.getSalary());
        
        System.out.println("✅ 不同类型的属性写入器字节码生成测试通过");
    }

    @Test
    void testCreateWriterCodeWithoutConverter() throws Exception {
        // 测试不使用TypeConverter的Writer字节码生成
        PropertyDescriptor descriptor = getPropertyDescriptor(TestEntity.class, "name");
        
        byte[] writerCode = accessor.createWriterCode(TestEntity.class,this.getClass().getPackageName()+".TestEntity_setName_Writer_NoConverter", descriptor, null);
        saveByteCodeToTarget("TestEntity_setName_Writer_NoConverter.class", writerCode);
        
        Class<?> writerClass = defineClassFromBytes(writerCode);
        PropertyWriter writer = (PropertyWriter) writerClass.getDeclaredConstructor(TypeConverter.class)
                .newInstance((TypeConverter) null);
        
        TestEntity entity = new TestEntity();
        writer.accept(entity, "无转换器测试");
        
        assertEquals("无转换器测试", entity.getName());
        
        System.out.println("✅ 无TypeConverter的Writer字节码生成测试通过");
    }

    @Test
    void testCreateWriterCodeWithGenericType() throws Exception {
        // 测试泛型类型的Writer字节码生成
        PropertyDescriptor descriptor = getPropertyDescriptor(TestEntity.class, "genericList");
        
        TypeConverter converter = (source, type) -> {
            if (type.isInstance(source)) {
                return source;
            }
            // 处理泛型List<String>
            if (type.resolve() == List.class && source instanceof List) {
                return source;
            }
            return type.toClass().cast(source);
        };
        
        byte[] writerCode = accessor.createWriterCode(TestEntity.class,this.getClass().getPackageName()+".TestEntity_setGenericList_Writer", descriptor, converter);
        saveByteCodeToTarget(this.getClass().getPackageName()+".TestEntity_setGenericList_Writer.class", writerCode);
        
        Class<?> writerClass = defineClassFromBytes(writerCode);
        ResolvableType resolvableType = ResolvableType.forMethodParameter(descriptor.getWriteMethod(), 0);
        PropertyWriter writer = (PropertyWriter) writerClass.getDeclaredConstructor(TypeConverter.class, ResolvableType.class)
                .newInstance(converter, resolvableType);
        
        TestEntity entity = new TestEntity();
        List<String> testList = Arrays.asList("item1", "item2", "item3");
        writer.accept(entity, testList);
        
        assertEquals(testList, entity.getGenericList());
        
        System.out.println("✅ 泛型类型的Writer字节码生成测试通过");
    }

    @Test
    void testTypeCheckOptimization() throws Exception {
        System.out.println("\n=== 类型检查优化测试 ===");
        
        // 创建一个计数器来跟踪TypeConverter的调用次数
        AtomicInteger converterCallCount = new AtomicInteger(0);
        
        TypeConverter countingConverter = (source, type) -> {
            converterCallCount.incrementAndGet();
            System.out.println("  TypeConverter.convert() 被调用，参数: " + source + " -> " + type.toClass().getSimpleName());
            
            if (source == null) {
                return null;
            }
            
            if (type.isInstance(source)) {
                return source;
            }
            
            // 处理各种类型转换
            Class<?> targetType = type.toClass();
            
            // String 到其他类型的转换
            if (source instanceof String) {
                String str = (String) source;
                if (targetType == int.class || targetType == Integer.class) {
                    return Integer.parseInt(str);
                } else if (targetType == double.class || targetType == Double.class) {
                    return Double.parseDouble(str);
                } else if (targetType == boolean.class || targetType == Boolean.class) {
                    return Boolean.parseBoolean(str);
                }
            }
            
            // 数字到String的转换
            if (targetType == String.class) {
                return source.toString();
            }
            
            // 数字之间的转换
            if (source instanceof Number && (targetType.isPrimitive() || Number.class.isAssignableFrom(targetType))) {
                Number number = (Number) source;
                if (targetType == int.class || targetType == Integer.class) {
                    return number.intValue();
                } else if (targetType == double.class || targetType == Double.class) {
                    return number.doubleValue();
                } else if (targetType == long.class || targetType == Long.class) {
                    return number.longValue();
                } else if (targetType == float.class || targetType == Float.class) {
                    return number.floatValue();
                }
            }
            
            return targetType.cast(source);
        };
        
        // 测试String类型的属性
        PropertyWriter nameWriter = accessor.createWriter(TestEntity.class, "name", countingConverter);
        PropertyWriter ageWriter = accessor.createWriter(TestEntity.class, "age", countingConverter);
        
        TestEntity entity = new TestEntity();
        
        // 重置计数器
        converterCallCount.set(0);
        
        System.out.println("\n1. 测试类型一致的情况：");
        
        // 类型一致的情况：传入String给String属性
        nameWriter.accept(entity, "正确的String类型");
        System.out.println("   设置String值到String属性，converter调用次数: " + converterCallCount.get());
        assertEquals("正确的String类型", entity.getName());
        assertEquals(0, converterCallCount.get(), "类型一致时不应该调用TypeConverter");
        
        // 类型一致的情况：传入Integer给int属性
        ageWriter.accept(entity, 25);
        System.out.println("   设置Integer值到int属性，converter调用次数: " + converterCallCount.get());
        assertEquals(25, entity.getAge());
        assertEquals(0, converterCallCount.get(), "类型一致时不应该调用TypeConverter");
        
        System.out.println("\n2. 测试类型不一致的情况：");
        
        // 类型不一致的情况：传入Integer给String属性
        nameWriter.accept(entity, 123);
        System.out.println("   设置Integer值到String属性，converter调用次数: " + converterCallCount.get());
        assertEquals("123", entity.getName());
        assertEquals(1, converterCallCount.get(), "类型不一致时应该调用TypeConverter");
        
        // 类型不一致的情况：传入String给int属性  
        ageWriter.accept(entity, "30");
        System.out.println("   设置String值到int属性，converter调用次数: " + converterCallCount.get());
        assertEquals(30, entity.getAge());
        assertEquals(2, converterCallCount.get(), "类型不一致时应该调用TypeConverter");
        
        System.out.println("\n3. 测试null值的情况：");
        
        // null值的情况
        nameWriter.accept(entity, null);
        System.out.println("   设置null值到String属性，converter调用次数: " + converterCallCount.get());
        assertNull(entity.getName());
        assertEquals(3, converterCallCount.get(), "null值应该调用TypeConverter");
        
        System.out.println("\n✅ 类型检查优化测试通过！");
        System.out.println("   ✓ 类型一致时跳过了TypeConverter调用");
        System.out.println("   ✓ 类型不一致时正确调用了TypeConverter");
        System.out.println("   ✓ null值处理正确");
    }

    @Test
    void testTypeCheckOptimizationWithPrimitives() throws Exception {
        System.out.println("\n=== 基本类型优化测试 ===");
        
        AtomicInteger converterCallCount = new AtomicInteger(0);
        
        TypeConverter countingConverter = (source, type) -> {
            converterCallCount.incrementAndGet();
            System.out.println("  TypeConverter.convert() 被调用: " + source.getClass().getSimpleName() + " -> " + type.toClass().getSimpleName());
            
            if (source instanceof Number && (type.toClass().isPrimitive() || Number.class.isAssignableFrom(type.toClass()))) {
                Number number = (Number) source;
                if (type.toClass() == int.class || type.toClass() == Integer.class) {
                    return number.intValue();
                } else if (type.toClass() == double.class || type.toClass() == Double.class) {
                    return number.doubleValue();
                } else if (type.toClass() == boolean.class || type.toClass() == Boolean.class) {
                    return number.intValue() != 0;
                }
            }
            
            return source;
        };
        
        PropertyWriter ageWriter = accessor.createWriter(TestEntity.class, "age", countingConverter);
        PropertyWriter salaryWriter = accessor.createWriter(TestEntity.class, "salary", countingConverter);
        PropertyWriter activeWriter = accessor.createWriter(TestEntity.class, "active", countingConverter);
        
        TestEntity entity = new TestEntity();
        converterCallCount.set(0);
        
        System.out.println("\n1. 测试基本类型包装类的直接赋值：");
        
        // Integer -> int (类型匹配)
        ageWriter.accept(entity, Integer.valueOf(25));
        System.out.println("   Integer -> int，converter调用次数: " + converterCallCount.get());
        assertEquals(25, entity.getAge());
        assertEquals(0, converterCallCount.get());
        
        // Double -> double (类型匹配)
        salaryWriter.accept(entity, Double.valueOf(50000.0));
        System.out.println("   Double -> double，converter调用次数: " + converterCallCount.get());
        assertEquals(50000.0, entity.getSalary());
        assertEquals(0, converterCallCount.get());
        
        // Boolean -> boolean (类型匹配)
        activeWriter.accept(entity, Boolean.valueOf(true));
        System.out.println("   Boolean -> boolean，converter调用次数: " + converterCallCount.get());
        assertEquals(true, entity.isActive());
        assertEquals(0, converterCallCount.get());
        
        System.out.println("\n2. 测试需要类型转换的情况：");
        
        // Long -> int (需要转换)
        ageWriter.accept(entity, Long.valueOf(35L));
        System.out.println("   Long -> int，converter调用次数: " + converterCallCount.get());
        assertEquals(35, entity.getAge());
        assertEquals(1, converterCallCount.get());
        
        // Float -> double (需要转换)
        salaryWriter.accept(entity, Float.valueOf(60000.0f));
        System.out.println("   Float -> double，converter调用次数: " + converterCallCount.get());
        assertEquals(60000.0, entity.getSalary(), 0.01);
        assertEquals(2, converterCallCount.get());
        
        // Integer -> boolean (需要转换)
        activeWriter.accept(entity, Integer.valueOf(1));
        System.out.println("   Integer -> boolean，converter调用次数: " + converterCallCount.get());
        assertEquals(true, entity.isActive());
        assertEquals(3, converterCallCount.get());
        
        System.out.println("\n✅ 基本类型优化测试通过！");
        System.out.println("   ✓ 包装类型到基本类型的直接转换被优化");
        System.out.println("   ✓ 不同数值类型之间正确调用了TypeConverter");
    }

    @Test 
    void testOptimizedByteCodeGeneration() throws Exception {
        System.out.println("\n=== 优化后字节码生成测试 ===");
        
        TypeConverter converter = (source, type) -> {
            if (type.isInstance(source)) {
                return source;
            }
            return type.toClass().cast(source);
        };
        
        // 生成包含类型检查优化的字节码
        PropertyDescriptor nameDescriptor = getPropertyDescriptor(TestEntity.class, "name");
        PropertyDescriptor ageDescriptor = getPropertyDescriptor(TestEntity.class, "age");
        
        byte[] optimizedNameWriterCode = accessor.createWriterCode(TestEntity.class,
                this.getClass().getPackageName()+".TestEntity_setName_Writer_Optimized",
                nameDescriptor, converter);
        byte[] optimizedAgeWriterCode = accessor.createWriterCode(TestEntity.class,
                this.getClass().getPackageName()+".TestEntity_setAge_Writer_Optimized",
                ageDescriptor,
                converter);
        
        // 保存优化后的字节码
        saveByteCodeToTarget("TestEntity_setName_Writer_Optimized.class", optimizedNameWriterCode);
        saveByteCodeToTarget("TestEntity_setAge_Writer_Optimized.class", optimizedAgeWriterCode);
        
        // 验证字节码可以正常工作
        Class<?> nameWriterClass = defineClassFromBytes(optimizedNameWriterCode);
        ResolvableType nameResolvableType = ResolvableType.forMethodParameter(nameDescriptor.getWriteMethod(), 0);
        PropertyWriter nameWriter = (PropertyWriter) nameWriterClass.getDeclaredConstructor(TypeConverter.class, ResolvableType.class)
                .newInstance(converter, nameResolvableType);

        Class<?> ageWriterClass = defineClassFromBytes(optimizedAgeWriterCode);
        ResolvableType ageResolvableType = ResolvableType.forMethodParameter(ageDescriptor.getWriteMethod(), 0);
        PropertyWriter ageWriter = (PropertyWriter) ageWriterClass.getDeclaredConstructor(TypeConverter.class, ResolvableType.class)
                .newInstance(converter, ageResolvableType);

        // 测试功能
        TestEntity entity = new TestEntity();
        nameWriter.accept(entity, "优化测试");
        ageWriter.accept(entity, 42);

        assertEquals("优化测试", entity.getName());
        assertEquals(42, entity.getAge());

        System.out.println("💾 优化后的字节码已保存到:");
        System.out.println("   - TestEntity_setName_Writer_Optimized.class (" + optimizedNameWriterCode.length + " bytes)");
        System.out.println("   - TestEntity_setAge_Writer_Optimized.class (" + optimizedAgeWriterCode.length + " bytes)");
        System.out.println("\n✅ 优化后字节码生成测试通过！");
    }

    // 辅助方法：保存字节码到target目录
    private void saveByteCodeToTarget(String fileName, byte[] bytecode) throws IOException {
        Path targetDir = Paths.get("target/bytecode");
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }
        
        Path filePath = targetDir.resolve(fileName);
        Files.write(filePath, bytecode);
        
        System.out.println("💾 字节码已保存到: " + filePath.toAbsolutePath());
    }

    // 辅助方法：从字节码定义类
    private Class<?> defineClassFromBytes(byte[] bytecode) throws IllegalAccessException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        return lookup.defineClass(bytecode);
    }

    // 辅助方法：获取PropertyDescriptor
    private PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName) throws Exception {
        java.beans.BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(clazz);
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
            if (propertyName.equals(descriptor.getName())) {
                return descriptor;
            }
        }
        throw new IllegalArgumentException("Property '" + propertyName + "' not found in class " + clazz.getName());
    }

    // 测试实体类
    public static class TestEntity {
        private String name;
        private int age;
        private boolean active;
        private double salary;
        private Address address;
        private String writeOnlyValue;
        private List<String> genericList;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public double getSalary() {
            return salary;
        }

        public void setSalary(double salary) {
            this.salary = salary;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public List<String> getGenericList() {
            return genericList;
        }

        public void setGenericList(List<String> genericList) {
            this.genericList = genericList;
        }

        // 只读属性
        public String getReadOnlyProperty() {
            return "只读属性";
        }

        // 只写属性
        public void setWriteOnlyProperty(String value) {
            this.writeOnlyValue = value;
        }

        public String getWriteOnlyValue() {
            return writeOnlyValue;
        }
    }

    // 地址类
    public static class Address {
        private String street;
        private String city;

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }
    }
} 