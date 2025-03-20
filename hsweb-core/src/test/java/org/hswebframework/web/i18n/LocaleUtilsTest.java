package org.hswebframework.web.i18n;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Locale;

import static org.junit.Assert.*;

public class LocaleUtilsTest {

    @Test
    public void testSupports(){

        assertNotNull(LocaleUtils.getSupportLocales());

        System.out.println(LocaleUtils.getSupportLocales());


    }

    @Test
    public void testFlux() {
        Flux.just(1)
            .as(LocaleUtils::transform)
            .doOnNext(i -> {
                assertEquals(i.intValue(), 1);
                assertEquals(LocaleUtils.current(), Locale.ENGLISH);
            })
            .contextWrite(LocaleUtils.useLocale(Locale.ENGLISH))
            .blockLast();
    }

    @Test
    public void testMono() {
        Mono.just(1)
            .doOnNext(i -> {
                assertEquals(i.intValue(), 1);
                assertEquals(LocaleUtils.current(), Locale.ENGLISH);
            })
            .as(LocaleUtils::transform)
            .contextWrite(LocaleUtils.useLocale(Locale.ENGLISH))
            .block();

        LocaleUtils
                .doInReactive(()->{
                    assertEquals(LocaleUtils.current(), Locale.ENGLISH);
                    return null;
                })
                .contextWrite(LocaleUtils.useLocale(Locale.ENGLISH))
                .block();
    }

}