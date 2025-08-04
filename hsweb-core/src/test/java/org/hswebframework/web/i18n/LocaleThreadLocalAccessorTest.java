package org.hswebframework.web.i18n;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Schedulers;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class LocaleThreadLocalAccessorTest {

    static {
        Hooks.enableAutomaticContextPropagation();
    }

    @Test
    void testInReactive() {

        for (Locale availableLocale : Locale.getAvailableLocales()) {
            assertEquals(availableLocale,
                         LocaleUtils.doWith(
                             availableLocale,
                             () -> LocaleUtils
                                 .currentReactive()
                                 .subscribeOn(Schedulers.boundedElastic())
                                 .block()));
        }


    }
}