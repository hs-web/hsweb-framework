package org.hswebframework.web.exception;

import org.hswebframework.web.i18n.LocaleUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

/**
 * 支持溯源的异常,通过{@link TraceSourceException#withSource(Object) }来标识异常的源头.
 * 在捕获异常的地方通过获取异常源来处理一些逻辑,比如判断是由哪条数据发生的错误等操作.
 *
 * @author zhouhao
 * @since 4.0.15
 */
public class TraceSourceException extends RuntimeException {

    private static final String deepTraceKey = TraceSourceException.class.getName() + "_deep";
    private static final Context deepTraceContext = Context.of(deepTraceKey, true);

    private String operation;

    private Object source;

    public TraceSourceException() {

    }

    public TraceSourceException(String message) {
        super(message);
    }

    public TraceSourceException(Throwable e) {
        super(e);
    }

    public TraceSourceException(String message, Throwable e) {
        super(message, e);
    }

    @Nullable
    public Object getSource() {
        return source;
    }

    @Nullable
    public String getOperation() {
        return operation;
    }

    public TraceSourceException withSource(Object source) {
        this.source = source;
        return self();
    }

    public TraceSourceException withSource(String operation, Object source) {
        this.operation = operation;
        this.source = source;
        return self();
    }

    protected TraceSourceException self() {
        return this;
    }

    /**
     * 深度溯源上下文,用来标识是否是深度溯源的异常.开启深度追踪后,会创建新的{@link  TraceSourceException}对象.
     *
     * @return 上下文
     * @see reactor.core.publisher.Flux#subscriberContext(Context)
     * @see Mono#subscriberContext(Context)
     */
    public static Context deepTraceContext() {
        return deepTraceContext;
    }

    public static <T> Function<Throwable, Mono<T>> transfer(Object source) {
        return transfer(null, source);
    }


    /**
     * 溯源异常转换器.通常配合{@link  Mono#onErrorResume(Function)}使用.
     * <p>
     * 转换逻辑:
     * <p>
     * 1. 如果捕获的异常不是TraceSourceException,则直接创建新的TraceSourceException并返回.
     * <p>
     * 2. 如果捕获的异常是TraceSourceException,并且上下文没有指定{@link TraceSourceException#deepTraceContext()},
     * 则修改捕获的TraceSourceException异常中的source.如果上下文中指定了{@link TraceSourceException#deepTraceContext()}
     * 则创建新的TraceSourceException
     *
     * <pre>{@code
     *
     *  doSomething()
     *  .onErrorResume(TraceSourceException.transfer(data))
     *
     * }</pre>
     *
     * @param operation 操作名称
     * @param source    源
     * @param <T>       泛型
     * @return 转换器
     * @see reactor.core.publisher.Flux#onErrorResume(Function)
     * @see Mono#onErrorResume(Function)
     */
    public static <T> Function<Throwable, Mono<T>> transfer(String operation, Object source) {
        if (source == null && operation == null) {
            return Mono::error;
        }
        return err -> {
            if (err instanceof TraceSourceException) {
                return Mono
                        .deferWithContext(ctx -> {
                            if (ctx.hasKey(deepTraceKey)) {
                                return Mono.error(new TraceSourceException(err).withSource(operation,source));
                            } else {
                                return Mono.error(((TraceSourceException) err).withSource(operation,source));
                            }
                        });
            }
            return Mono.error(new TraceSourceException(err).withSource(operation,source));
        };
    }

    public static Object tryGetSource(Throwable err) {
        if (err instanceof TraceSourceException) {
            return ((TraceSourceException) err).getSource();
        }
        return null;
    }

    public static String tryGetOperation(Throwable err) {
        if (err instanceof TraceSourceException) {
            return ((TraceSourceException) err).getOperation();
        }
        return null;
    }

    public static String tryGetOperationLocalized(Throwable err, Locale locale) {
        String opt = tryGetOperation(err);
        return StringUtils.hasText(opt) ? LocaleUtils.resolveMessage(opt, locale, opt) : opt;
    }

    public static Mono<String> tryGetOperationLocalizedReactive(Throwable err) {
        return LocaleUtils
                .currentReactive()
                .handle((locale, sink) -> {
                    String opt = tryGetOperationLocalized(err, locale);
                    if (opt != null) {
                        sink.next(opt);
                    }
                });
    }
}
