package org.hswebframework.web.dictionary.configuration;

import org.hswebframework.web.dictionary.service.CompositeDictDefineRepository;
import org.hswebframework.web.dictionary.service.DefaultDictionaryItemService;
import org.hswebframework.web.dictionary.service.DefaultDictionaryService;
import org.hswebframework.web.dictionary.webflux.WebfluxDictionaryController;
import org.hswebframework.web.dictionary.webflux.WebfluxDictionaryItemController;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
@EnableConfigurationProperties(DictionaryProperties.class)
public class DictionaryAutoConfiguration {


    @AutoConfiguration
    static class DictionaryServiceConfiguration {

        @Bean
        public DefaultDictionaryItemService defaultDictionaryItemService() {
            return new DefaultDictionaryItemService();
        }

        @Bean
        public DefaultDictionaryService defaultDictionaryService() {
            return new DefaultDictionaryService();
        }

        @Bean
        public CompositeDictDefineRepository compositeDictDefineRepository(DictionaryProperties properties) {
            CompositeDictDefineRepository repository = new CompositeDictDefineRepository();

            properties
                .doScanEnum()
                .map(CompositeDictDefineRepository::parseEnumDict)
                .forEach(repository::addDefine);

            return repository;
        }
    }


    @AutoConfiguration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class DictionaryWebFluxConfiguration {

        @Bean
        public WebfluxDictionaryController webfluxDictionaryController() {
            return new WebfluxDictionaryController();
        }

        @Bean
        public WebfluxDictionaryItemController webfluxDictionaryItemController() {
            return new WebfluxDictionaryItemController();
        }
    }
}
