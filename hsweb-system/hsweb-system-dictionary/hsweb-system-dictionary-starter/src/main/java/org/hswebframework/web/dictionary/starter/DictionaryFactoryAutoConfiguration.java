package org.hswebframework.web.dictionary.starter;

import org.hswebframework.web.service.dictionary.builder.DictionaryParserBuilder;
import org.hswebframework.web.service.dictionary.simple.builder.SimpleDictionaryParserBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 */
@Configuration
@ComponentScan({"org.hswebframework.web.service.dictionary.simple"
        , "org.hswebframework.web.controller.dictionary"})
public class DictionaryFactoryAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(DictionaryParserBuilder.class)
    public DictionaryParserBuilder dictionaryParserBuilder() {
        return new SimpleDictionaryParserBuilder();
    }
}
