package org.hswebframework.web.bean.accessor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.ResolvableType;

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
        // 性能测试
        PropertyReader nameReader = accessor.createReader(TestEntity.class, "name");
        PropertyWriter nameWriter = accessor.createWriter(TestEntity.class, "name", null);

        TestEntity entity = new TestEntity();
        entity.setName("初始名称");

        int iterations = 1000000; // 增加到100万次以获得更准确的结果

        // 测试ASM访问器性能
        long asmStartTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            nameReader.apply(entity);
            nameWriter.accept(entity, "ASM名称" + i);
        }
        long asmEndTime = System.nanoTime();
        long asmDuration = asmEndTime - asmStartTime;

        // 验证ASM结果
        assertEquals("ASM名称" + (iterations - 1), entity.getName());

        // 重置实体状态
        entity.setName("初始名称");

        // 测试直接方法调用性能
        long directStartTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            entity.getName(); // 直接调用getter
            entity.setName("直接名称" + i); // 直接调用setter
        }
        long directEndTime = System.nanoTime();
        long directDuration = directEndTime - directStartTime;

        // 验证直接调用结果
        assertEquals("直接名称" + (iterations - 1), entity.getName());

        // 输出性能对比结果
        System.out.println("=== 性能测试结果 (执行" + iterations + "次读写操作) ===");
        System.out.println("ASM访问器耗时: " + asmDuration / 1000000 + "ms");
        System.out.println("直接方法调用耗时: " + directDuration / 1000000 + "ms");
        System.out.println("性能比率: " + String.format("%.2f", (double) asmDuration / directDuration) + "x");

        if (asmDuration < directDuration * 10) {
            System.out.println("✅ ASM访问器性能优秀，与直接调用相差不大");
        } else {
            System.out.println("⚠️ ASM访问器性能有待优化");
        }

        // 性能断言 - ASM访问器应该不超过直接调用的10倍
        assertTrue(asmDuration < directDuration * 10,
                "ASM访问器性能不应该比直接调用慢10倍以上。ASM: " + asmDuration / 1000000 + "ms, 直接调用: " + directDuration / 1000000 + "ms");

        // 绝对性能要求 - 应该在合理时间内完成
        assertTrue(asmDuration < 5000000000L, "ASM访问器执行时间不应超过5秒"); // 5秒
        assertTrue(directDuration < 1000000000L, "直接方法调用执行时间不应超过1秒"); // 1秒
    }

    @Test
    void testDetailedPerformanceComparison() {
        System.out.println("\n=== 详细性能分析测试 ===");

        PropertyReader nameReader = accessor.createReader(TestEntity.class, "name");
        PropertyWriter nameWriter = accessor.createWriter(TestEntity.class, "name", null);

        TestEntity entity = new TestEntity();
        int warmupIterations = 100000;  // 热身次数
        int testIterations = 1000000;   // 正式测试次数

        // 1. JVM 热身 - 让两种方式都充分优化
        System.out.println("1. 开始 JVM 热身...");

        // 热身 ASM 访问器
        for (int i = 0; i < warmupIterations; i++) {
            nameReader.apply(entity);
            nameWriter.accept(entity, entity.getName() + i);
        }

        // 热身直接调用
        for (int i = 0; i < warmupIterations; i++) {

            entity.setName(entity.getName() + i);
        }

        System.out.println("   热身完成");

        // 2. 多轮测试以获得更稳定的结果
        long[] asmTimes = new long[5];
        long[] directTimes = new long[5];

        for (int round = 0; round < 5; round++) {
            System.out.println("2. 执行第 " + (round + 1) + " 轮测试...");

            // 重置状态
            entity.setName("测试" + round);

            // 测试 ASM 访问器
            long asmStart = System.nanoTime();
            for (int i = 0; i < testIterations; i++) {
                String result = (String) nameReader.apply(entity);
                nameWriter.accept(entity, "asm_" + i);
            }
            long asmEnd = System.nanoTime();
            asmTimes[round] = asmEnd - asmStart;

            // 强制垃圾回收
            System.gc();
            Thread.yield();

            // 重置状态
            entity.setName("测试" + round);

            // 测试直接调用
            long directStart = System.nanoTime();
            for (int i = 0; i < testIterations; i++) {
                String result = entity.getName();
                entity.setName("direct_" + i);
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
        assertTrue(asmAvg < directAvg * 3, "ASM访问器性能不应该比直接调用慢3倍以上");
    }

    // 测试实体类
    public static class TestEntity {
        private String name;
        private int age;
        private boolean active;
        private double salary;
        private Address address;
        private String writeOnlyValue;

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