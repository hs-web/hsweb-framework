package org.hswebframework.web.message.redis;

import org.hswebframework.web.message.*;
import org.redisson.api.RedissonClient;

/**
 * @author zhouhao
 */
public class RedissonMessager implements Messager {

    private RedissonClient redisson;

    public RedissonMessager(RedissonClient redisson) {
        this.redisson = redisson;
    }

    public void setRedisson(RedissonClient redisson) {
        this.redisson = redisson;
    }

    @Override
    public MessagePublish publish(Message message) {
        return new RedissonMessagePublish(redisson, message);
    }

    @Override
    public <M extends Message> MessageSubscribe<M> subscribe(MessageSubject subscribe) {
        return new RedissionMessageSubscribe<>(subscribe, redisson);
    }
}
