package org.hswebframework.web.exception;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * 支持溯源的异常,通过{@link TraceSourceException#withSource(Object) }来标识异常的源头.
 * 在捕获异常的地方通过获取异常源来处理一些逻辑,比如判断是由哪条数据发生的错误等操作.
 *
 * @author zhouhao
 * @since 4.0.15
 */
public class TraceSourceException extends RuntimeException {

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

    public Optional<Object> sourceOptional() {
        return Optional.ofNullable(source);
    }

    @Nullable
    public Object getSource() {
        return source;
    }

    public TraceSourceException withSource(Object source) {
        this.source = source;
        return self();
    }

    protected TraceSourceException self() {
        return this;
    }
}
