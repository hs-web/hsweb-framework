package org.hswebframework.web.starter.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.dict.EnumDict;
import org.hswebframework.web.i18n.LocaleUtils;
import org.hswebframework.web.i18n.MessageSourceInitializer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;
import java.util.function.Predicate;

public class CustomJackson2jsonEncoderTest {


    @Before
    public void init(){
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setDefaultEncoding("utf-8");
        messageSource.setBasenames("i18n.messages");
        MessageSourceInitializer.init(messageSource);
    }

    @Test
    public void testI18n() {

        doTest(new TestEntity(TestEnum.e1),Locale.forLanguageTag("en-US"),s->s.contains("Option1"));
        doTest(new TestEntity(TestEnum.e1),Locale.forLanguageTag("zh-CN"),s->s.contains("选项1"));

    }

    public void doTest(TestEntity entity, Locale locale, Predicate<String> verify){

        CustomJackson2jsonEncoder encoder = new CustomJackson2jsonEncoder(new ObjectMapper());

        encoder.encode(Mono.just(entity),
                       new DefaultDataBufferFactory(),
                       ResolvableType.forType(TestEntity.class),
                       MediaType.APPLICATION_JSON,
                       Collections.emptyMap())
               .as(DataBufferUtils::join)
               .map(buf -> buf.toString(StandardCharsets.UTF_8))
               .doOnNext(System.out::println)
               .contextWrite(LocaleUtils.useLocale(locale))
               .as(StepVerifier::create)
               .expectNextMatches(verify)
               .verifyComplete();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestEntity {

        private TestEnum testEnum;
    }


    @Getter
    @AllArgsConstructor
    public enum TestEnum implements EnumDict<String> {
        e1("enum.e1"),
        e2("enum.e2");

        private final String text;

        @Override
        public String getValue() {
            return name();
        }

    }
}