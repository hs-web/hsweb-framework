package org.hswebframework.web.i18n;

import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.Locale;

import static org.junit.Assert.*;

public class LocaleUtilsTest {


    @Test
    public void testOnNext() {
        Flux.just(1)
            .as(LocaleUtils.doOnNext((i, l) -> {
                assertEquals(i.intValue(), 1);
                assertEquals(l, Locale.CHINA);
            }))
            .subscriberContext(LocaleUtils.useLocale(Locale.CHINA))
            .blockLast();
    }


}