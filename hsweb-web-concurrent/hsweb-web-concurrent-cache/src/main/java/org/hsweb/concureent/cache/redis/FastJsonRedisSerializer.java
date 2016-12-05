package org.hsweb.concureent.cache.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * @author zhouhao
 * @TODO
 */
public class FastJsonRedisSerializer implements RedisSerializer<Object> {
    @Override
    public byte[] serialize(Object o) throws SerializationException {
        if (o == null) return null;
        return JSON.toJSONBytes(o, SerializerFeature.WriteClassName);
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) return null;
        return JSON.parse(bytes);
    }
}
