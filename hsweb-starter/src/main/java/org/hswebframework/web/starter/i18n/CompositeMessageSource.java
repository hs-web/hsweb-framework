package org.hswebframework.web.starter.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import jakarta.annotation.Nonnull;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class CompositeMessageSource implements MessageSource {

    private final List<MessageSource> messageSources = new CopyOnWriteArrayList<>();

    public void addMessageSources(Collection<MessageSource> source) {
        messageSources.addAll(source);
    }

    public void addMessageSource(MessageSource source) {
        messageSources.add(source);
    }

    private String formatMessage(String message, Object[] args, Locale locale) {
        if (message == null || message.isBlank() || args == null || args.length == 0) {
            return message;
        }
        MessageFormat messageFormat = new MessageFormat(message, locale);
        return messageFormat.format(args);
    }

    @Override
    public String getMessage(@Nonnull String code, Object[] args, String defaultMessage, @Nonnull Locale locale) {
        for (MessageSource messageSource : messageSources) {
            String result = messageSource.getMessage(code, args, null, locale);
            if (StringUtils.hasText(result)) {
                return formatMessage(result, args, locale);
            }
        }
        return formatMessage(defaultMessage, args, locale);
    }

    @Override
    @Nonnull
    public String getMessage(@Nonnull String code, Object[] args, @Nonnull Locale locale) throws NoSuchMessageException {
        for (MessageSource messageSource : messageSources) {
            try {
                String result = messageSource.getMessage(code, args, locale);
                if (StringUtils.hasText(result)) {
                    return formatMessage(result, args, locale);
                }
            } catch (NoSuchMessageException ignore) {

            }
        }
        throw new NoSuchMessageException(code, locale);
    }

    @Override
    @Nonnull
    public String getMessage(@Nonnull MessageSourceResolvable resolvable, @Nonnull Locale locale) throws NoSuchMessageException {
        for (MessageSource messageSource : messageSources) {
            try {
                String result = messageSource.getMessage(resolvable, locale);
                if (StringUtils.hasText(result)) {
                    return formatMessage(result, resolvable.getArguments(), locale);
                }
            } catch (NoSuchMessageException ignore) {

            }
        }
        String[] codes = resolvable.getCodes();
        throw new NoSuchMessageException(!ObjectUtils.isEmpty(codes) ? codes[codes.length - 1] : "", locale);
    }
}
