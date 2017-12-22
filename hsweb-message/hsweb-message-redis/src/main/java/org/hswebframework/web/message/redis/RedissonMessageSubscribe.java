package org.hswebframework.web.message.redis;

import org.hswebframework.web.message.Message;
import org.hswebframework.web.message.MessageSubject;
import org.hswebframework.web.message.MessageSubscribe;
import org.hswebframework.web.message.support.QueueMessageSubject;
import org.hswebframework.web.message.support.TopicMessageSubject;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RQueue;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author zhouhao
 */
public class RedissonMessageSubscribe<M extends Message> implements MessageSubscribe<M> {
    private MessageSubject subject;
    private RedissonClient redisson;
    private boolean           running    = false;
    private int               listenerId = 0;
    private List<Consumer<M>> consumers  = new ArrayList<>();
    private RTopic<M> topic;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public RedissonMessageSubscribe(MessageSubject subject, RedissonClient redisson) {
        this.subject = subject;
        this.redisson = redisson;
    }

    public RedissonMessageSubscribe(RedissonClient redisson) {
        this.redisson = redisson;
    }

    @Override
    public MessageSubscribe<M> onMessage(Consumer<M> consumer) {
        consumers.add(consumer);
        if (!running) {
            doRun();
        }
        return this;
    }

    @Override
    public void cancel() {
        running = false;
        if (listenerId != 0 && topic != null) {
            topic.removeListener(listenerId);
            topic = null;
        }
        consumers.clear();
    }

    private static Codec codec = JsonJacksonCodec.INSTANCE;

    private void doRun() {
        if (subject instanceof QueueMessageSubject) {
            String queueName = ((QueueMessageSubject) subject).getQueueName();
            RQueue<M> queue = redisson.getQueue(queueName, codec);
            RCountDownLatch countDownLatch = redisson.getCountDownLatch("cdl_" + queueName);
            Thread thread = new Thread(() -> {
                while (running) {
                    try {
                        if (redisson.isShutdown() || redisson.isShuttingDown()) {
                            return;
                        }
                        countDownLatch.trySetCount(1);
                        countDownLatch.await();
                        consumers.forEach(cons -> {
                            M message = queue.poll();
                            if (null != message) {
                                cons.accept(message);
                            }
                        });
                    } catch (InterruptedException e) {
                        running = false;
                        logger.error("queue consumer thread interrupted", e);
                        Thread.currentThread().interrupt();
                    }
                }
            });
            running = true;
            thread.start();
            return;
        }
        if (subject instanceof TopicMessageSubject) {
            topic = redisson.getTopic("topic_" + ((TopicMessageSubject) subject).getTopic(), codec);
            listenerId = topic.addListener((channel, msg) -> consumers.forEach(cons -> cons.accept(msg)));
        }
        running = true;
    }
}
