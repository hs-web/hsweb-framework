package org.hswebframework.web.dictionary.starter;

import org.hswebframework.web.dao.Dao;
import org.hswebframework.web.dict.DictDefineRepository;
import org.hswebframework.web.dict.DictSupportApi;
import org.hswebframework.web.dict.defaults.DefaultDictSupportApi;
import org.hswebframework.web.dictionary.api.builder.DictionaryParserBuilder;
import org.hswebframework.web.dictionary.simple.builder.SimpleDictionaryParserBuilder;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 */
@Configuration
@ComponentScan({"org.hswebframework.web.dictionary.simple"
        , "org.hswebframework.web.controller.dictionary"})
@MapperScan(value = "org.hswebframework.web.dictionary.simple.dao",markerInterface = Dao.class)
public class DictionaryFactoryAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(DictionaryParserBuilder.class)
    public DictionaryParserBuilder dictionaryParserBuilder() {
        return new SimpleDictionaryParserBuilder();
    }

    @Bean
    public DictSupportApi dictSupportApi(DictDefineRepository repository) {
        return new DefaultDictSupportApi(repository);
    }
}
