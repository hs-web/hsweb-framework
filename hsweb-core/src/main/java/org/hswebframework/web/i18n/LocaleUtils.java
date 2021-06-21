package org.hswebframework.web.i18n;

import org.hswebframework.web.exception.I18nSupportException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LocaleUtils {

    public static final LocaleContext DEFAULT_CONTEXT = new SimpleLocaleContext(Locale.getDefault());

    public static Mono<LocaleContext> reactive() {
        return Mono
                .subscriberContext()
                .map(ctx -> ctx
                        .<LocaleContext>getOrEmpty(LocaleContext.class)
                        .orElse(DEFAULT_CONTEXT));
    }


    public static <S extends I18nSupportException, R> Mono<R> resolveThrowable(MessageSource messageSource,
                                                                               S source,
                                                                               BiFunction<S, String, R> mapper) {
        return doWithReactive(messageSource, source, Throwable::getMessage, mapper, source.getArgs());
    }

    public static <S extends Throwable, R> Mono<R> resolveThrowable(MessageSource messageSource,
                                                                    S source,
                                                                    BiFunction<S, String, R> mapper,
                                                                    Object... args) {
        return doWithReactive(messageSource, source, Throwable::getMessage, mapper, args);
    }

    public static <S, R> Mono<R> doWithReactive(MessageSource messageSource,
                                                S source,
                                                Function<S, String> message,
                                                BiFunction<S, String, R> mapper,
                                                Object... args) {
        return reactive()
                .map(ctx -> {
                    String msg = message.apply(source);
                    String newMsg = resolveMessage(messageSource, msg, ctx.getLocale(), msg, args);
                    return mapper.apply(source, newMsg);
                });
    }

    public static Mono<String> reactiveMessage(MessageSource messageSource,
                                               String code,
                                               Object... args) {
        return reactive()
                .map(ctx -> resolveMessage(messageSource, code, ctx.getLocale(), code, args));
    }

    public static String resolveMessage(MessageSource messageSource,
                                        String code,
                                        Locale locale,
                                        String defaultMessage,
                                        Object... args) {
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }

}
