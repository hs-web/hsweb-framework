package org.hsweb.web.core.session.redis;

import org.hsweb.web.core.session.HttpSessionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;

import javax.annotation.Resource;

/**
 * Created by zhouhao on 16-5-27.
 */
@Configuration
@ConditionalOnBean(value = RedisOperationsSessionRepository.class, name = "sessionRedisTemplate")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RedisHttpSessionManagerConfiguration {

    @Resource(name = "sessionRedisTemplate")
    RedisTemplate sessionRedisTemplate;

    @Bean
    public HttpSessionManager sessionListener(RedisOperationsSessionRepository repository) {
        RedisHttpSessionManager redisHttpSessionManager = new RedisHttpSessionManager();
        redisHttpSessionManager.setSessionRedisTemplate(sessionRedisTemplate);
        redisHttpSessionManager.setRedisOperationsSessionRepository(repository);
        return redisHttpSessionManager;
    }
}
