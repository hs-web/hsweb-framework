package org.hswebframework.web.message.redis;

import org.hswebframework.web.message.Message;
import org.hswebframework.web.message.MessagePublish;
import org.hswebframework.web.message.MessageSubject;
import org.hswebframework.web.message.support.MultipleUserMessageSubject;
import org.hswebframework.web.message.support.TopicMessageSubject;
import org.hswebframework.web.message.support.UserMessageSubject;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RQueue;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.codec.SerializationCodec;

import java.util.function.Consumer;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class RedissonMessagePublish implements MessagePublish {
    private MessageSubject to;
    private RedissonClient redissonClient;
    private Message        message;

    public RedissonMessagePublish(RedissonClient redissonClient, Message message) {
        this.redissonClient = redissonClient;
        this.message = message;
    }

    @Override
    public MessagePublish to(MessageSubject subject) {
        this.to = subject;
        return this;
    }

    private boolean useQueue() {
        return to instanceof UserMessageSubject || to instanceof MultipleUserMessageSubject;
    }

    private SerializationCodec codec = new SerializationCodec();

    private Consumer<String> queueConsumer = id -> {
        RQueue<Message> queue = redissonClient.getQueue("queue_user_" + id, codec);
        RCountDownLatch downLatch = redissonClient.getCountDownLatch("cdl_user_" + id);
        queue.add(message);
        downLatch.countDown();
    };

    @Override
    public void send() {
        if (redissonClient.isShutdown() || redissonClient.isShuttingDown()) {
            return;
        }
        if (to instanceof UserMessageSubject) {
            queueConsumer.accept(((UserMessageSubject) to).getUserId());
        }
        if (to instanceof MultipleUserMessageSubject) {
            ((MultipleUserMessageSubject) to).getUserIdList().forEach(queueConsumer);
        }
        if (to instanceof TopicMessageSubject) {
            RTopic<Message> topic = redissonClient.getTopic("topic_" + ((TopicMessageSubject) to).getTopic(), codec);
            topic.publish(message);
        }
    }


}
