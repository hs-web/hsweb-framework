package org.hswebframework.web.starter.dictionary;

import org.hswebframework.web.service.dictionary.builder.DictionaryParserBuilder;
import org.hswebframework.web.service.dictionary.simple.builder.SimpleDictionaryParserBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Configuration
public class DictionaryFactoryAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(DictionaryParserBuilder.class)
    public DictionaryParserBuilder dictionaryParserBuilder() {
        return new SimpleDictionaryParserBuilder();
    }
}
