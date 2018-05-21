package org.hswebframework.web.message;

import org.hswebframework.web.message.memory.MemoryMessager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Configuration
@ConditionalOnMissingBean(Messager.class)
public class MessagerAutoConfiguration {
    @Bean
    public Messager messager() {
        return new MemoryMessager();
    }
}
