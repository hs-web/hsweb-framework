package org.hswebframework.web.async;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 异步任务执行异常
 * @author zhouhao
 */
public class AsyncJobException extends RuntimeException {
    private static final long serialVersionUID = 4003904073250315538L;
    private List<Exception> errors;

    public AsyncJobException(List<Exception> errors) {
       this(errors.size()>0?errors.get(0).getMessage():null,errors);
    }

    public AsyncJobException(String message, List<Exception> errors) {
        super(message,errors.size()>0?errors.get(0):null);
        this.errors = errors;
    }

    public List<Exception> getErrors() {
        return errors;
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        if (null != errors) {
            List<StackTraceElement> stackTraceElements = errors.stream()
                    .map(Exception::getStackTrace)
                    .flatMap(Stream::of)
                    .collect(Collectors.toList());
            stackTraceElements.addAll(Arrays.asList(super.getStackTrace()));
            return stackTraceElements.toArray(new StackTraceElement[stackTraceElements.size()]);
        }
        return super.getStackTrace();
    }
}
