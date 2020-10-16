package org.hswebframework.web.oauth2.server;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

public class RedisHelper {

    public static LettuceConnectionFactory factory;

    static {
        factory = new LettuceConnectionFactory(new RedisStandaloneConfiguration("127.0.0.1"));
        factory.afterPropertiesSet();
    }
}
