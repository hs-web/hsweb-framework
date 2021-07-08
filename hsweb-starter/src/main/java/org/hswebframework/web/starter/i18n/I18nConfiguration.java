package org.hswebframework.web.starter.i18n;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.i18n.MessageSourceInitializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class I18nConfiguration {

    @Bean
    @SneakyThrows
    public MessageSource autoResolveI18nMessageSource() {

        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:i18n/**");

        for (Resource resource : resources) {
            String path = resource.getURL().getPath();
            if (StringUtils.hasText(path) && (path.endsWith(".properties") || path.endsWith(".xml"))) {
                path = path.substring(path.lastIndexOf("i18n"));
                String[] split = path.split("[/|\\\\]");
                String name = split[split.length - 1];
                name = name.contains("_") ? name.substring(0, name.indexOf("_")) : name;
                split[split.length - 1] = name;
                log.info("register i18n message resource {} -> {}", path, name);

                messageSource.addBasenames(String.join("/", split));
            }
        }
        return messageSource;
    }

    @Bean
    @Primary
    public MessageSource compositeMessageSource(ObjectProvider<MessageSource> objectProvider) {
        CompositeMessageSource messageSource = new CompositeMessageSource();
        messageSource.addMessageSources(objectProvider.stream().collect(Collectors.toList()));
        MessageSourceInitializer.init(messageSource);
        return messageSource;
    }


}
