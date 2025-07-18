package org.hswebframework.web.recycler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RecyclerImpl 单元测试")
class RecyclerImplTest {

    private AtomicInteger createCount;
    private AtomicInteger resetCount;
    private Supplier<StringBuilder> factory;
    private Consumer<StringBuilder> rest;

    @BeforeEach
    void setUp() {
        createCount = new AtomicInteger(0);
        resetCount = new AtomicInteger(0);
        
        factory = () -> {
            createCount.incrementAndGet();
            return new StringBuilder();
        };
        
        rest = sb -> {
            resetCount.incrementAndGet();
            sb.setLength(0);
        };
    }

    @Test
    @DisplayName("构造函数参数验证")
    void testConstructorValidation() {
        // 测试 size 参数验证
        assertThrows(IllegalArgumentException.class, 
                () -> new RecyclerImpl<>(-1, factory, rest));
        assertThrows(IllegalArgumentException.class, 
                () -> new RecyclerImpl<>(0, factory, rest));
        assertThrows(IllegalArgumentException.class, 
                () -> new RecyclerImpl<>(1, factory, rest));
        
        // 测试 factory 参数验证
        assertThrows(IllegalArgumentException.class, 
                () -> new RecyclerImpl<>(2, null, rest));
        
        // 测试 rest 参数验证
        assertThrows(IllegalArgumentException.class, 
                () -> new RecyclerImpl<>(2, factory, null));
    }

    @Test
    @DisplayName("正常构造函数")
    void testValidConstructor() {
        assertDoesNotThrow(() -> new RecyclerImpl<>(2, factory, rest));
        assertDoesNotThrow(() -> new RecyclerImpl<>(10, factory, rest));
    }

    @Test
    @DisplayName("测试 doWith(Function) 方法")
    void testDoWithFunction() {
        RecyclerImpl<StringBuilder> recycler = new RecyclerImpl<>(2, factory, rest);
        
        // 第一次调用，应该创建新对象
        String result1 = recycler.doWith(sb -> {
            sb.append("hello");
            return sb.toString();
        });
        
        assertEquals("hello", result1);
        assertEquals(1, createCount.get());
        assertEquals(1, resetCount.get());
        
        // 第二次调用，在非阻塞线程中可能重用 ThreadLocal 对象
        String result2 = recycler.doWith(sb -> {
            sb.append("world");
            return sb.toString();
        });
        
        assertEquals("world", result2);
        // 由于 ThreadLocal 的存在，可能只创建一个对象
        assertTrue(createCount.get() >= 1);
        assertTrue(resetCount.get() >= 1);
    }

    @Test
    @DisplayName("测试 doWith(BiFunction) 方法")
    void testDoWithBiFunction() {
        RecyclerImpl<StringBuilder> recycler = new RecyclerImpl<>(2, factory, rest);
        
        // 第一次调用，应该创建新对象
        String result1 = recycler.doWith("hello", (sb, arg) -> {
            sb.append(arg);
            return sb.toString();
        });
        
        assertEquals("hello", result1);
        assertEquals(1, createCount.get());
        assertEquals(1, resetCount.get());
        
        // 第二次调用，在非阻塞线程中可能重用 ThreadLocal 对象
        String result2 = recycler.doWith("world", (sb, arg) -> {
            sb.append(arg);
            return sb.toString();
        });
        
        assertEquals("world", result2);
        // 由于 ThreadLocal 的存在，可能只创建一个对象
        assertTrue(createCount.get() >= 1);
        assertTrue(resetCount.get() >= 1);
    }

    @Test
    @DisplayName("测试队列大小限制")
    void testQueueSizeLimit() {
        RecyclerImpl<StringBuilder> recycler = new RecyclerImpl<>(2, factory, rest);
        
        // 在非阻塞线程环境中，测试 ThreadLocal 的行为
        if (Schedulers.isInNonBlockingThread()) {
            // 在非阻塞线程中，应该优先使用 ThreadLocal
            String result1 = recycler.doWith(sb -> {
                sb.append("test1");
                return sb.toString();
            });
            
            assertEquals("test1", result1);
            assertEquals(1, createCount.get()); // 创建了一个 ThreadLocal 对象
            assertEquals(1, resetCount.get());
            
            // 第二次调用，应该重用 ThreadLocal 对象
            String result2 = recycler.doWith(sb -> {
                sb.append("test2");
                return sb.toString();
            });
            
            assertEquals("test2", result2);
            assertEquals(1, createCount.get()); // 还是只有一个对象
            assertEquals(2, resetCount.get());
        } else {
            // 在阻塞线程中，测试队列的行为
            for (int i = 0; i < 5; i++) {
                final int iteration = i;
                recycler.doWith(sb -> {
                    sb.append("test").append(iteration);
                    return sb.toString();
                });
            }
            
            // 由于队列大小限制，不会创建过多对象
            assertTrue(createCount.get() <= 3); // 最多创建3个对象（2个队列 + 1个临时）
            assertEquals(5, resetCount.get()); // 每次都重置
        }
    }

    @Test
    @DisplayName("测试异常情况下的资源清理")
    void testExceptionHandling() {
        RecyclerImpl<StringBuilder> recycler = new RecyclerImpl<>(2, factory, rest);
        
        // 测试在执行过程中抛出异常
        assertThrows(RuntimeException.class, () -> {
            recycler.doWith(sb -> {
                sb.append("test");
                throw new RuntimeException("Test exception");
            });
        });
        
        // 验证对象被正确重置和回收
        assertEquals(1, createCount.get());
        assertEquals(1, resetCount.get());
        
        // 验证下次调用可以正常重用对象
        String result = recycler.doWith(sb -> {
            sb.append("after_exception");
            return sb.toString();
        });
        
        assertEquals("after_exception", result);
        assertTrue(resetCount.get() >= 1);
    }

    @Test
    @DisplayName("测试并发安全性")
    void testConcurrency() throws InterruptedException {
        RecyclerImpl<StringBuilder> recycler = new RecyclerImpl<>(10, factory, rest);
        
        Thread[] threads = new Thread[5];
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    final int iteration = j;
                    String result = recycler.doWith(sb -> {
                        sb.append("thread").append(threadId).append("-").append(iteration);
                        return sb.toString();
                    });
                    if (result.startsWith("thread" + threadId)) {
                        successCount.incrementAndGet();
                    }
                }
            });
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        assertEquals(500, successCount.get());
        // 由于 ThreadLocal 的存在，每个线程最多创建一个对象，加上队列中的对象
        assertTrue(createCount.get() <= 15);
        assertEquals(500, resetCount.get()); // 每次使用后都重置
    }

    @Test
    @DisplayName("测试 Recycler 静态工厂方法")
    void testStaticFactory() {
        Recycler<StringBuilder> recycler = Recycler.create(factory, rest, 2);
        
        String result = recycler.doWith(sb -> {
            sb.append("factory_test");
            return sb.toString();
        });
        
        assertEquals("factory_test", result);
        assertEquals(1, createCount.get());
        assertEquals(1, resetCount.get());
    }

    @Test
    @DisplayName("测试 ThreadLocal 重用逻辑")
    void testThreadLocalReuse() {
        RecyclerImpl<StringBuilder> recycler = new RecyclerImpl<>(2, factory, rest);
        
        // 第一次调用
        String result1 = recycler.doWith(sb -> {
            sb.append("first");
            return sb.toString();
        });
        
        assertEquals("first", result1);
        int initialCreateCount = createCount.get();
        int initialResetCount = resetCount.get();
        
        // 第二次调用，应该重用对象
        String result2 = recycler.doWith(sb -> {
            sb.append("second");
            return sb.toString();
        });
        
        assertEquals("second", result2);
        // 验证对象被重用
        assertTrue(resetCount.get() > initialResetCount);
    }
} 