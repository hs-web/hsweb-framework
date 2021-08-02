package org.hswebframework.web.i18n;

import org.hswebframework.web.exception.I18nSupportException;
import org.reactivestreams.Publisher;
import org.springframework.context.MessageSource;
import reactor.core.publisher.Flux;
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
 * 常用方法:
 *
 * <ul>
 *  <li>{@link LocaleUtils#current()} </li>
 *  <li>{@link LocaleUtils#currentReactive()}</li>
 *  <li>{@link LocaleUtils#resolveMessageReactive(String, Object...)}</li>
 *  <li>{@link LocaleUtils#doOnNext(BiConsumer)}</li>
 * </ul>
 *
 * @author zhouhao
 * @since 4.0.11
 */
public final class LocaleUtils {

    public static final Locale DEFAULT_LOCALE = Locale.getDefault();

    private static final ThreadLocal<Locale> CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    static MessageSource messageSource = UnsupportedMessageSource.instance();

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
     * 在指定的区域中执行函数,<b>只能</b>在非响应式同步操作时使用,如：转换实体类中某些属性的国际化消息。
     * <p>
     * 在函数的逻辑中可以通过{@link LocaleUtils#current()}来获取当前语言.
     *
     * @param data   参数
     * @param locale 区域
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

    /**
     * 使用指定的区域来执行某些操作
     *
     * @param locale   区域
     * @param consumer 任务
     */
    public static void doWith(Locale locale, Consumer<Locale> consumer) {
        try {
            CONTEXT_THREAD_LOCAL.set(locale);
            consumer.accept(locale);
        } finally {
            CONTEXT_THREAD_LOCAL.remove();
        }
    }

    /**
     * 在响应式作用,使用指定的区域作为语言环境,在下游则可以使用{@link LocaleUtils#currentReactive()}来获取
     * <p>
     * <pre>
     * monoOrFlux
     * .subscriberContext(LocaleUtils.useLocale(locale))
     * </pre>
     *
     * @param locale 区域
     * @return 上下为构造函数
     */
    public static Function<Context, Context> useLocale(Locale locale) {
        return ctx -> ctx.put(Locale.class, locale);
    }

    /**
     * 响应式方式获取当前区域
     *
     * @return 区域
     */
    @SuppressWarnings("all")
    public static Mono<Locale> currentReactive() {
        return Mono
                .subscriberContext()
                .map(ctx -> ctx.getOrDefault(Locale.class, DEFAULT_LOCALE));
    }

    /**
     * 响应式方式解析出异常的区域消息，并进行结果转换.
     * <p>
     *
     * <pre>
     * LocaleUtils
     *  .resolveThrowable(error,(err,msg)-> createResponse(err,msg) );
     * </pre>
     *
     * @param source 异常
     * @param mapper 结果转换器
     * @param <S>    异常类型
     * @param <R>    转换结果类型
     * @return 转换后的结果
     * @see LocaleUtils#doWithReactive(Object, Function, BiFunction, Object...)
     */
    public static <S extends I18nSupportException, R> Mono<R> resolveThrowable(S source,
                                                                               BiFunction<S, String, R> mapper) {
        return resolveThrowable(messageSource, source, mapper);
    }

    /**
     * 指定消息源,响应式方式解析出异常的区域消息，并进行结果转换.
     * <p>
     *
     * <pre>
     * LocaleUtils
     *  .resolveThrowable(source,error,(err,msg)-> createResponse(err,msg) );
     * </pre>
     *
     * @param messageSource 消息源
     * @param source        异常
     * @param mapper        结果转换器
     * @param <S>           异常类型
     * @param <R>           转换结果类型
     * @return 转换后的结果
     * @see LocaleUtils#doWithReactive(Object, Function, BiFunction, Object...)
     */
    public static <S extends I18nSupportException, R> Mono<R> resolveThrowable(MessageSource messageSource,
                                                                               S source,
                                                                               BiFunction<S, String, R> mapper) {
        return doWithReactive(messageSource, source, I18nSupportException::getI18nCode, mapper, source.getArgs());
    }

    /**
     * 使用参数,响应式方式解析出异常的区域消息，并进行结果转换.
     * <p>
     * 参数对应消息模版中的{n}
     * <p>
     *
     * <pre>
     * LocaleUtils
     *  .resolveThrowable(source,error,(err,msg)-> createResponse(err,msg) );
     * </pre>
     *
     * @param source 异常
     * @param mapper 结果转换器
     * @param args   参数
     * @param <S>    异常类型
     * @param <R>    转换结果类型
     * @return 转换后的结果
     * @see LocaleUtils#doWithReactive(Object, Function, BiFunction, Object...)
     * @see java.text.MessageFormat
     */
    public static <S extends Throwable, R> Mono<R> resolveThrowable(S source,
                                                                    BiFunction<S, String, R> mapper,
                                                                    Object... args) {
        return resolveThrowable(messageSource, source, mapper, args);
    }

    /**
     * 使用参数,指定消息源,响应式方式解析出异常的区域消息，并进行结果转换.
     * <p>
     * 参数对应消息模版中的{n}
     * <p>
     *
     * <pre>
     * LocaleUtils
     *  .resolveThrowable(source,error,(err,msg)-> createResponse(err,msg) );
     * </pre>
     *
     * @param source 异常
     * @param mapper 结果转换器
     * @param args   参数
     * @param <S>    异常类型
     * @param <R>    转换结果类型
     * @return 转换后的结果
     * @see LocaleUtils#doWithReactive(Object, Function, BiFunction, Object...)
     * @see java.text.MessageFormat
     */
    public static <S extends Throwable, R> Mono<R> resolveThrowable(MessageSource messageSource,
                                                                    S source,
                                                                    BiFunction<S, String, R> mapper,
                                                                    Object... args) {
        return doWithReactive(messageSource, source, Throwable::getMessage, mapper, args);
    }

    /**
     * 在响应式环境中处理区域消息并转换为新的结果
     *
     * @param source  数据
     * @param message 消息转换
     * @param mapper  数据转换
     * @param args    参数
     * @param <S>     数据类型
     * @param <R>     结果类型
     * @return 转换结果
     * @see java.text.MessageFormat
     */
    public static <S, R> Mono<R> doWithReactive(S source,
                                                Function<S, String> message,
                                                BiFunction<S, String, R> mapper,
                                                Object... args) {
        return doWithReactive(messageSource, source, message, mapper, args);
    }

    /**
     * 指定消息源，在响应式环境中处理区域消息并转换为新的结果
     *
     * @param source  数据
     * @param message 消息转换
     * @param mapper  数据转换
     * @param args    参数
     * @param <S>     数据类型
     * @param <R>     结果类型
     * @return 转换结果
     * @see java.text.MessageFormat
     */
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

    /**
     * 使用默认的消息源，响应式方式解析消息
     *
     * @param code 消息编码
     * @param args 参数
     * @return 解析后的消息
     */
    public static Mono<String> resolveMessageReactive(String code,
                                                      Object... args) {
        return currentReactive()
                .map(locale -> resolveMessage(messageSource, locale, code, code, args));
    }

    /**
     * 使用指定的消息源，响应式方式解析消息
     *
     * @param messageSource 消息源
     * @param code          消息编码
     * @param args          参数
     * @return 解析后的消息
     */
    public static Mono<String> resolveMessageReactive(MessageSource messageSource,
                                                      String code,
                                                      Object... args) {
        return currentReactive()
                .map(locale -> resolveMessage(messageSource, locale, code, code, args));
    }

    /**
     * 解析消息
     *
     * @param code           消息编码
     * @param locale         地区
     * @param defaultMessage 默认消息
     * @param args           参数
     * @return 解析后的消息
     */
    public static String resolveMessage(String code,
                                        Locale locale,
                                        String defaultMessage,
                                        Object... args) {
        return resolveMessage(messageSource, locale, code, defaultMessage, args);
    }

    /**
     * 使用指定的消息源解析消息
     *
     * @param messageSource
     * @param code           消息编码
     * @param locale         地区
     * @param defaultMessage 默认消息
     * @param args           参数
     * @return 解析后的消息
     */
    public static String resolveMessage(MessageSource messageSource,
                                        Locale locale,
                                        String code,
                                        String defaultMessage,
                                        Object... args) {
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }

    /**
     * 使用默认消息源和当前地区解析消息
     *
     * @param code 消息编码
     * @param args 参数
     * @return 解析后的消息
     */
    public static String resolveMessage(String code, Object... args) {
        return resolveMessage(messageSource, current(), code, code, args);
    }

    /**
     * 使用默认消息源和当前地区解析消息
     *
     * @param code           消息编码
     * @param args           参数
     * @param defaultMessage 默认消息
     * @return 解析后的消息
     */
    public static String resolveMessage(String code,
                                        String defaultMessage,
                                        Object... args) {
        return resolveMessage(messageSource, current(), code, defaultMessage, args);
    }

    /**
     * 使用指定消息源和当前地区解析消息
     *
     * @param code 消息编码
     * @param args 参数
     * @return 解析后的消息
     */
    public static String resolveMessage(MessageSource messageSource,
                                        String code,
                                        String defaultMessage,
                                        Object... args) {
        return resolveMessage(messageSource, current(), code, defaultMessage, args);
    }


    /**
     * 在响应式中获取区域并执行指定的操作
     *
     * @param operation 操作
     * @param <T>       元素类型
     */
    public static <T> Consumer<Signal<T>> on(SignalType type, BiConsumer<Signal<T>, Locale> operation) {
        return signal -> {
            if (signal.getType() != type) {
                return;
            }
            Locale locale = signal.getContext().getOrDefault(Locale.class, DEFAULT_LOCALE);

            doWith(locale, l -> operation.accept(signal, l));
        };
    }

    /**
     * 在响应式的各个周期获取地区并执行指定的操作
     *
     * <pre>
     *     monoOrFlux
     *     .as(LocaleUtils.doOn(ON_NEXT,(signal,locale)-> ... ))
     *     ...
     * </pre>
     *
     * @param type      周期类型
     * @param operation 操作
     * @param <E>       响应式流中元素类型
     * @param <T>       响应式流类型
     * @return 原始流
     */
    @SuppressWarnings("all")
    public static <E, T extends Publisher<E>> Function<T, T> doOn(SignalType type, BiConsumer<Signal<E>, Locale> operation) {
        return publisher -> {
            if (publisher instanceof Mono) {
                return (T) Mono
                        .from(publisher)
                        .doOnEach(on(type, operation));
            }
            return (T) Flux
                    .from(publisher)
                    .doOnEach(on(type, operation));
        };
    }

    /**
     * <pre>
     * monoOrFlux
     * .as(LocaleUtils.doOnNext(element-> .... ))
     * ...
     * </pre>
     */
    public static <E, T extends Publisher<E>> Function<T, T> doOnNext(Consumer<E> operation) {
        return doOn(SignalType.ON_NEXT, (s, l) -> operation.accept(s.get()));
    }

    /**
     * <pre>
     * monoOrFlux
     * .as(LocaleUtils.doOnNext((element,locale)-> .... ))
     * ...
     * </pre>
     */
    public static <E, T extends Publisher<E>> Function<T, T> doOnNext(BiConsumer<E, Locale> operation) {
        return doOn(SignalType.ON_NEXT, (s, l) -> operation.accept(s.get(), l));
    }

    /**
     * <pre>
     * monoOrFlux
     * .as(LocaleUtils.doOnError(error-> .... ))
     * ...
     * </pre>
     */
    public static <E, T extends Publisher<E>> Function<T, T> doOnError(Consumer<Throwable> operation) {
        return doOn(SignalType.ON_ERROR, (s, l) -> operation.accept(s.getThrowable()));
    }

    /**
     * <pre>
     * monoOrFlux
     * .as(LocaleUtils.doOnError((error,locale)-> .... ))
     * ...
     * </pre>
     */
    public static <E, T extends Publisher<E>> Function<T, T> doOnError(BiConsumer<Throwable, Locale> operation) {
        return doOn(SignalType.ON_ERROR, (s, l) -> operation.accept(s.getThrowable(), l));
    }

}
