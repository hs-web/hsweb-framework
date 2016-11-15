package org.hsweb.web.core.session.redis;

import org.hsweb.web.core.session.HttpSessionManager;
import org.hsweb.web.core.session.HttpSessionManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;

import javax.annotation.Resource;
import java.util.List;

@Configuration
@ConditionalOnBean(value = RedisOperationsSessionRepository.class, name = "sessionRedisTemplate")
@ConditionalOnWebApplication
public class RedisHttpSessionManagerConfiguration {

    @Resource(name = "sessionRedisTemplate")
    private RedisTemplate sessionRedisTemplate;

    @Bean(name = "httpSessionManager")
    public HttpSessionManager redisHttpSessionManager(RedisOperationsSessionRepository repository) {
        RedisHttpSessionManager redisHttpSessionManager = new RedisHttpSessionManager();
        redisHttpSessionManager.setSessionRedisTemplate(sessionRedisTemplate);
        redisHttpSessionManager.setRedisOperationsSessionRepository(repository);
        return redisHttpSessionManager;
    }
}
