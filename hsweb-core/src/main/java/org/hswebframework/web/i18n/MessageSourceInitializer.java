package org.hswebframework.web.i18n;

import org.springframework.context.MessageSource;

public class MessageSourceInitializer {

    public static void init(MessageSource messageSource) {
        if (LocaleUtils.messageSource == null) {
            LocaleUtils.messageSource = messageSource;
        }
    }
}
