package org.hswebframework.web.message.redis;

import org.hswebframework.web.message.Message;
import org.hswebframework.web.message.MessagePublish;
import org.hswebframework.web.message.MessageSubject;
import org.hswebframework.web.message.support.MultipleQueueMessageSubject;
import org.hswebframework.web.message.support.QueueMessageSubject;
import org.hswebframework.web.message.support.TopicMessageSubject;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RQueue;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
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
        return to instanceof QueueMessageSubject || to instanceof MultipleQueueMessageSubject;
    }

    private static Codec codec = JsonJacksonCodec.INSTANCE;

    private Consumer<String> queueConsumer = queueName -> {
        RQueue<Message> queue = redissonClient.getQueue(queueName, codec);
        RCountDownLatch downLatch = redissonClient.getCountDownLatch("cdl_" + queueName);
        queue.add(message);
        downLatch.countDown();
    };

    @Override
    public void send() {
        if (redissonClient.isShutdown() || redissonClient.isShuttingDown()) {
            return;
        }
        if (to instanceof QueueMessageSubject) {
            queueConsumer.accept(((QueueMessageSubject) to).getQueueName());
        }
        if (to instanceof MultipleQueueMessageSubject) {
            ((MultipleQueueMessageSubject) to).getQueueName().forEach(queueConsumer);
        }
        if (to instanceof TopicMessageSubject) {
            RTopic<Message> topic = redissonClient.getTopic("topic_" + ((TopicMessageSubject) to).getTopic(), codec);
            topic.publish(message);
        }
    }
}
