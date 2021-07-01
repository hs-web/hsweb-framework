package org.hswebframework.web.i18n;

import org.hswebframework.web.exception.I18nSupportException;
import org.springframework.context.MessageSource;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.core.publisher.SignalType;
import reactor.util.context.Context;

import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 用于进行国际化消息转换
 *
 * @author zhouhao
 * @since 4.0.11
 */
public class LocaleUtils {

    public static final Locale DEFAULT_LOCALE = Locale.getDefault();

    private static final ThreadLocal<Locale> CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    static MessageSource messageSource;

    /**
     * 获取当前的语言地区,如果没有设置则返回系统默认语言
     *
     * @return Locale
     */
    public static Locale current() {
        Locale locale = CONTEXT_THREAD_LOCAL.get();
        if (locale == null) {
            locale = DEFAULT_LOCALE;
        }
        return locale;
    }

    /**
     * 在指定的语言环境中执行函数,<b>只能</b>在非响应式同步操作时使用,如：转换实体类中某些属性的国际化消息。
     * <p>
     * 在函数的逻辑中可以通过{@link LocaleUtils#current()}来获取当前语言.
     *
     * @param data   参数
     * @param locale 语言地区
     * @param mapper 函数
     * @param <T>    参数类型
     * @param <R>    函数返回类型
     * @return 返回值
     */
    public static <T, R> R doWith(T data, Locale locale, BiFunction<T, Locale, R> mapper) {
        try {
            CONTEXT_THREAD_LOCAL.set(locale);
            return mapper.apply(data, locale);
        } finally {
            CONTEXT_THREAD_LOCAL.remove();
        }
    }

    public static Function<Context, Context> useLocale(Locale locale) {
        return ctx -> ctx.put(Locale.class, locale);
    }

    public static void doWith(Locale locale, Consumer<Locale> consumer) {
        try {
            CONTEXT_THREAD_LOCAL.set(locale);
            consumer.accept(locale);
        } finally {
            CONTEXT_THREAD_LOCAL.remove();
        }
    }

    /**
     * 响应式方式获取当前语言地区
     *
     * @return 语言地区
     */
    @SuppressWarnings("all")
    public static Mono<Locale> currentReactive() {
        return Mono
                .subscriberContext()
                .map(ctx -> ctx.getOrDefault(Locale.class, DEFAULT_LOCALE));
    }

    public static <T> void onNext(Signal<T> signal, BiConsumer<T, Locale> consumer) {
        if (signal.getType() != SignalType.ON_NEXT) {
            return;
        }
        Locale locale = signal.getContext().getOrDefault(Locale.class, DEFAULT_LOCALE);

        doWith(locale, l -> consumer.accept(signal.get(), l));

    }

    public static <S extends I18nSupportException, R> Mono<R> resolveThrowable(S source,
                                                                               BiFunction<S, String, R> mapper) {
        return resolveThrowable(messageSource, source, mapper);
    }

    public static <S extends I18nSupportException, R> Mono<R> resolveThrowable(MessageSource messageSource,
                                                                               S source,
                                                                               BiFunction<S, String, R> mapper) {
        return doWithReactive(messageSource, source, I18nSupportException::getCode, mapper, source.getArgs());
    }

    public static <S extends Throwable, R> Mono<R> resolveThrowable(S source,
                                                                    BiFunction<S, String, R> mapper,
                                                                    Object... args) {
        return resolveThrowable(messageSource, source, mapper, args);
    }

    public static <S extends Throwable, R> Mono<R> resolveThrowable(MessageSource messageSource,
                                                                    S source,
                                                                    BiFunction<S, String, R> mapper,
                                                                    Object... args) {
        return doWithReactive(messageSource, source, Throwable::getMessage, mapper, args);
    }

    public static <S, R> Mono<R> doWithReactive(S source,
                                                Function<S, String> message,
                                                BiFunction<S, String, R> mapper,
                                                Object... args) {
        return doWithReactive(messageSource, source, message, mapper, args);
    }

    public static <S, R> Mono<R> doWithReactive(MessageSource messageSource,
                                                S source,
                                                Function<S, String> message,
                                                BiFunction<S, String, R> mapper,
                                                Object... args) {
        return currentReactive()
                .map(locale -> {
                    String msg = message.apply(source);
                    String newMsg = resolveMessage(messageSource, locale, msg, msg, args);
                    return mapper.apply(source, newMsg);
                });
    }

    public static Mono<String> resolveMessageReactive(MessageSource messageSource,
                                                      String code,
                                                      Object... args) {
        return currentReactive()
                .map(locale -> resolveMessage(messageSource, locale, code, code, args));
    }

    public static String resolveMessage(String code,
                                        Locale locale,
                                        String defaultMessage,
                                        Object... args) {
        return resolveMessage(messageSource, locale, code, defaultMessage, args);
    }

    public static String resolveMessage(MessageSource messageSource,
                                        Locale locale,
                                        String code,
                                        String defaultMessage,
                                        Object... args) {
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }

    public static String resolveMessage(String code, Object... args) {
        return resolveMessage(messageSource, current(), code, code, args);
    }

    public static String resolveMessage(String code,
                                        String defaultMessage,
                                        Object... args) {
        return resolveMessage(messageSource, current(), code, defaultMessage, args);
    }

    public static String resolveMessage(MessageSource messageSource,
                                        String code,
                                        String defaultMessage,
                                        Object... args) {
        return resolveMessage(messageSource, current(), code, defaultMessage, args);
    }

}
