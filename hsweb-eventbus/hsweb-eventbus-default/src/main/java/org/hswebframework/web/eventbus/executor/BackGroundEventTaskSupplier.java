package org.hswebframework.web.eventbus.executor;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.eventbus.EventListener;
import org.hswebframework.web.eventbus.EventListenerDefine;
import org.hswebframework.web.eventbus.annotation.EventMode;

import java.util.Queue;
import java.util.concurrent.*;

/**
 * @author zhouhao
 * @since 1.0
 */
@Slf4j
public class BackGroundEventTaskSupplier implements EventExecuteTaskSupplier {
    private static final Queue<Runnable> queue = new ConcurrentLinkedDeque<>();

    private static volatile boolean running   = true;
    private static volatile boolean executing = false;

    private static CyclicBarrier barrier = new CyclicBarrier(2);

    static {
        Thread thread = new Thread(() -> {
            while (running) {
                Runnable job = queue.poll();
                if (job != null) {
                    executing = true;
                    try {
                        job.run();
                    } catch (Exception e) {
                        log.error("执行事件执行失败", e);
                    }
                } else {
                    executing = false;
                    try {
                        barrier.await(10, TimeUnit.SECONDS);
                        barrier.reset();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        });
        thread.setDaemon(false);
        thread.setName("BackGroundEventExecutor");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> running = false));
        thread.start();
    }


    @Override
    public boolean isSupport(EventListenerDefine define) {
        return define.getEventMode() == EventMode.BACKGROUND;
    }


    @Override
    public EventExecuteTask get(EventListener listener, Object event) {
        return () -> {
            queue.add(() -> listener.onEvent(event));
            try {
                if (!executing) {
                    barrier.await();
                }
            } catch (Exception e) {
              //  e.printStackTrace();
            }
        };
    }
}
