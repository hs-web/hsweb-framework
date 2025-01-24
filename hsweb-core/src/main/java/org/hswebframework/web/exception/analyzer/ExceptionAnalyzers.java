package org.hswebframework.web.exception.analyzer;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 异常分析器,用于分析异常信息.使用{@link ExceptionAnalyzer}进行分析拓展.
 *
 * @author zhouhao
 * @see ExceptionAnalyzer
 * @since 4.0.18
 */
@Slf4j
public class ExceptionAnalyzers {

    private static final List<ExceptionAnalyzer> ANALYZER = new CopyOnWriteArrayList<>();

    private ExceptionAnalyzers() {

    }

    static {
        ServiceLoader.load(ExceptionAnalyzer.class).forEach(ANALYZER::add);
    }

    public static void addAnalyzer(ExceptionAnalyzer analyzer) {
        log.debug("add ExceptionAnalyzer:{}", analyzer);
        ANALYZER.add(analyzer);
    }

    public static boolean analyze(Throwable failure) {
        Throwable cause = failure;
        while (cause != null) {
            for (ExceptionAnalyzer _analyzer : ANALYZER) {
                if (_analyzer.analyze(cause)) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }

}
