package org.hswebframework.web.bean;

import org.junit.Assert;
import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class FastBeanCopierColdStartContentionTest {

    private static final List<Supplier<ColdTarget>> coldTargets = Arrays.asList(
        ColdTarget01::new,
        ColdTarget02::new,
        ColdTarget03::new,
        ColdTarget04::new,
        ColdTarget05::new,
        ColdTarget06::new,
        ColdTarget07::new,
        ColdTarget08::new,
        ColdTarget09::new,
        ColdTarget10::new,
        ColdTarget11::new,
        ColdTarget12::new,
        ColdTarget13::new,
        ColdTarget14::new,
        ColdTarget15::new,
        ColdTarget16::new
    );

    @Test
    public void shouldCopyConcurrentColdMapToBeanPairs() throws Exception {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        Assert.assertTrue("Thread contention monitoring is required", threadMXBean.isThreadContentionMonitoringSupported());

        boolean originalContentionMonitoring = threadMXBean.isThreadContentionMonitoringEnabled();
        if (!originalContentionMonitoring) {
            threadMXBean.setThreadContentionMonitoringEnabled(true);
        }

        try {
            int workerCount = coldTargets.size();
            CountDownLatch ready = new CountDownLatch(workerCount);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch completed = new CountDownLatch(workerCount);
            AtomicReference<Throwable> failure = new AtomicReference<>();
            Thread[] workers = new Thread[workerCount];

            for (int i = 0; i < workerCount; i++) {
                int index = i;
                workers[i] = new Thread(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        ColdTarget target = coldTargets.get(index).get();
                        FastBeanCopier.copy(source(index), target);
                        Assert.assertEquals("name-" + index, target.getName());
                        Assert.assertEquals(index, target.getSequence());
                        Assert.assertEquals(index % 2 == 0, target.isEnabled());
                    } catch (Throwable error) {
                        failure.compareAndSet(null, error);
                    } finally {
                        completed.countDown();
                    }
                }, "fast-bean-copier-cold-" + index);
                workers[i].start();
            }

            Assert.assertTrue("Workers did not become ready", ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            boolean classPoolContentionObserved = false;
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
            long[] workerIds = Arrays.stream(workers).mapToLong(Thread::getId).toArray();
            while (completed.getCount() > 0 && System.nanoTime() < deadline) {
                ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(workerIds);
                classPoolContentionObserved |= isWaitingForClassPool(threadInfos);
                Thread.sleep(1);
            }

            Assert.assertTrue("Workers did not complete", completed.await(1, TimeUnit.SECONDS));
            Assert.assertNull("Concurrent cold copier generation failed", failure.get());
            Assert.assertFalse(
                "Concurrent cold copier generation must not contend on javassist.ClassPool",
                classPoolContentionObserved
            );
        } finally {
            if (!originalContentionMonitoring) {
                threadMXBean.setThreadContentionMonitoringEnabled(false);
            }
        }
    }

    private static boolean isWaitingForClassPool(ThreadInfo[] threadInfos) {
        return Arrays.stream(threadInfos)
                     .filter(info -> info != null && info.getThreadState() == Thread.State.BLOCKED)
                     .map(ThreadInfo::getLockName)
                     .anyMatch(lockName -> lockName != null && lockName.startsWith("javassist.ClassPool@"));
    }

    private static Map<String, Object> source(int index) {
        Map<String, Object> source = new HashMap<>();
        source.put("name", "name-" + index);
        source.put("sequence", index);
        source.put("enabled", index % 2 == 0);
        return source;
    }

    public static class ColdTarget {
        private String name;
        private int sequence;
        private boolean enabled;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getSequence() {
            return sequence;
        }

        public void setSequence(int sequence) {
            this.sequence = sequence;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class ColdTarget01 extends ColdTarget {
    }

    public static class ColdTarget02 extends ColdTarget {
    }

    public static class ColdTarget03 extends ColdTarget {
    }

    public static class ColdTarget04 extends ColdTarget {
    }

    public static class ColdTarget05 extends ColdTarget {
    }

    public static class ColdTarget06 extends ColdTarget {
    }

    public static class ColdTarget07 extends ColdTarget {
    }

    public static class ColdTarget08 extends ColdTarget {
    }

    public static class ColdTarget09 extends ColdTarget {
    }

    public static class ColdTarget10 extends ColdTarget {
    }

    public static class ColdTarget11 extends ColdTarget {
    }

    public static class ColdTarget12 extends ColdTarget {
    }

    public static class ColdTarget13 extends ColdTarget {
    }

    public static class ColdTarget14 extends ColdTarget {
    }

    public static class ColdTarget15 extends ColdTarget {
    }

    public static class ColdTarget16 extends ColdTarget {
    }
}
