package org.hswebframework.web.starter.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public class CompositeMessageSource implements MessageSource {

    private final List<MessageSource> messageSources = new CopyOnWriteArrayList<>();

    public void addMessageSources(Collection<MessageSource> source) {
        messageSources.addAll(source);
    }

    public void addMessageSource(MessageSource source) {
        messageSources.add(source);
    }

    @Override
    public String getMessage(@Nonnull String code, Object[] args, String defaultMessage, @Nonnull Locale locale) {
        for (MessageSource messageSource : messageSources) {
            String result = messageSource.getMessage(code, args, defaultMessage, locale);
            if (StringUtils.hasText(result)) {
                return result;
            }
        }
        return null;
    }

    @Override
    @Nonnull
    public String getMessage(@Nonnull String code, Object[] args, @Nonnull Locale locale) throws NoSuchMessageException {
        for (MessageSource messageSource : messageSources) {
            try {
                String result = messageSource.getMessage(code, args, locale);
                if (StringUtils.hasText(result)) {
                    return result;
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
                    return result;
                }
            } catch (NoSuchMessageException ignore) {

            }
        }
        String[] codes = resolvable.getCodes();
        throw new NoSuchMessageException(!ObjectUtils.isEmpty(codes) ? codes[codes.length - 1] : "", locale);
    }
}
