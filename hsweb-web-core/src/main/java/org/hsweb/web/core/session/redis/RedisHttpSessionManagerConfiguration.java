package org.hsweb.web.core.session.redis;

import org.hsweb.web.core.session.HttpSessionManager;
import org.hsweb.web.core.session.HttpSessionManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zhouhao on 16-5-27.
 */
@Configuration
@ConditionalOnBean(value = RedisOperationsSessionRepository.class, name = "sessionRedisTemplate")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RedisHttpSessionManagerConfiguration {
    @Autowired(required = false)
    private List<HttpSessionManagerListener> httpSessionManagerListeners;

    @Resource(name = "sessionRedisTemplate")
    private RedisTemplate sessionRedisTemplate;

    @Bean
    public HttpSessionManager sessionListener(RedisOperationsSessionRepository repository) {
        RedisHttpSessionManager redisHttpSessionManager = new RedisHttpSessionManager();
        if (httpSessionManagerListeners != null) {
            redisHttpSessionManager.setListeners(httpSessionManagerListeners);
        }
        redisHttpSessionManager.setSessionRedisTemplate(sessionRedisTemplate);
        redisHttpSessionManager.setRedisOperationsSessionRepository(repository);
        return redisHttpSessionManager;
    }
}
