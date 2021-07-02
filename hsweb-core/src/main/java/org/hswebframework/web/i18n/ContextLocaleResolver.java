package org.hswebframework.web.i18n;

import org.hibernate.validator.spi.messageinterpolation.LocaleResolver;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolverContext;

import java.util.Locale;

public class ContextLocaleResolver implements LocaleResolver {
    @Override
    public Locale resolve(LocaleResolverContext context) {
        return LocaleUtils.current();
    }
}
