package org.hswebframework.web.exception.analyzer;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * 提供基础的异常分析器实现
 *
 * @author zhouhao
 * @since 4.0.18
 */
@Slf4j
public class ExceptionAnalyzerReporter implements ExceptionAnalyzer {

    private final List<Reporter> reporter = new CopyOnWriteArrayList<>();


    public static String wrapLog(String message) {
        char[] arr = new char[message.length() + 2];
        Arrays.fill(arr, '=');
        arr[0] = '\n';
        arr[arr.length - 1] = '\n';
        String line = new String(arr);
        return line + message + line;
    }

    protected void addReporter(Predicate<Throwable> predicate,
                               Consumer<Throwable> reporter) {
        this.reporter.add(new Reporter() {
            @Override
            public boolean predicate(Throwable error) {
                return predicate.test(error);
            }

            @Override
            public void report(Throwable error) {
                reporter.accept(error);
            }
        });
    }

    protected void addSimpleReporter(Pattern pattern, Consumer<Throwable> reporter) {

        addReporter((error) -> {
            if (error.getMessage() == null) {
                return pattern.matcher(error.toString()).matches();
            }
            return pattern.matcher(error.getMessage()).matches() || pattern.matcher(error.toString()).matches();
        }, reporter);
    }

    public boolean doReportException(Throwable failure) {
        Throwable cause = failure;
        while (cause != null) {
            for (Reporter _reporter : this.reporter) {
                if (_reporter.predicate(cause)) {
                    _reporter.report(cause);
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }

    @Override
    public boolean analyze(Throwable error) {
        return doReportException(error);
    }

    interface Reporter {

        boolean predicate(Throwable error);

        void report(Throwable error);

    }
}
