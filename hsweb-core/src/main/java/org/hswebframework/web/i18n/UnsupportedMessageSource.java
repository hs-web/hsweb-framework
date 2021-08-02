package org.hswebframework.web.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;

public class UnsupportedMessageSource implements MessageSource {

    private static final UnsupportedMessageSource INSTANCE = new UnsupportedMessageSource();

    public static MessageSource instance() {
        return INSTANCE;
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return defaultMessage;
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        return code;
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        return resolvable.getDefaultMessage();
    }
}
