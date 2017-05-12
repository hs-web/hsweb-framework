package org.hswebframework.web.message.redis;

import org.hswebframework.web.message.Message;
import org.hswebframework.web.message.MessageSubject;
import org.hswebframework.web.message.MessageSubscribe;
import org.hswebframework.web.message.support.TopicMessageSubject;
import org.hswebframework.web.message.support.UserMessageSubject;
import org.redisson.api.*;
import org.redisson.codec.SerializationCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author zhouhao
 */
public class RedissionMessageSubscribe<M extends Message> implements MessageSubscribe<M> {
    private MessageSubject iam;
    private RedissonClient redisson;

    private boolean running = false;

    private List<Consumer<M>> consumers = new ArrayList<>();

    public RedissionMessageSubscribe(MessageSubject iam, RedissonClient redisson) {
        this.iam = iam;
        this.redisson = redisson;
    }

    public RedissionMessageSubscribe(RedissonClient redisson) {
        this.redisson = redisson;
    }

    @Override
    public MessageSubscribe<M> iam(MessageSubject iam) {
        this.iam = iam;
        return this;
    }

    @Override
    public MessageSubscribe<M> onMessage(Consumer<M> consumer) {
        consumers.add(consumer);
        if (!running) {
            doRun();
        }
        return this;
    }

    private static SerializationCodec codec = new SerializationCodec();

    private void doRun() {
        if (iam instanceof UserMessageSubject) {
            RQueue<M> queue = redisson
                    .getQueue("queue_user_" + ((UserMessageSubject) iam).getUserId(), codec);
            RCountDownLatch countDownLatch = redisson.getCountDownLatch("cdl_user_" + ((UserMessageSubject) iam).getUserId());
            Thread thread = new Thread(() -> {
                while (running) {
                    try {
                        countDownLatch.trySetCount(1);
                        countDownLatch.await();
                        consumers.forEach(cons -> cons.accept(queue.peek()));
                    } catch (InterruptedException e) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });
            running = true;
            thread.start();
            return;
        }
        if (iam instanceof TopicMessageSubject) {
            RTopic<M> topic = redisson.getTopic("topic_" + ((TopicMessageSubject) iam).getTopic(), codec);
            topic.addListener((channel, msg) -> consumers.forEach(cons -> cons.accept(msg)));
        }
        running = true;
    }
}
