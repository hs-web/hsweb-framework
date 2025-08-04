package org.hswebframework.web.i18n;

import io.micrometer.context.ThreadLocalAccessor;

import javax.annotation.Nonnull;
import java.util.Locale;

public class LocaleThreadLocalAccessor implements ThreadLocalAccessor<Locale> {

    @Override
    @Nonnull
    public Object key() {
        return Locale.class;
    }

    @Override
    public Locale getValue() {
        return LocaleUtils.CONTEXT_THREAD_LOCAL.getIfExists();
    }

    @Override
    public void setValue() {
        LocaleUtils.CONTEXT_THREAD_LOCAL.remove();
    }

    @Override
    public void setValue(@Nonnull Locale value) {
        LocaleUtils.CONTEXT_THREAD_LOCAL.set(value);
    }
}
