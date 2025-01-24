package org.hswebframework.web.exception.analyzer;

/**
 * 异常分析器,用于分析异常信息. 实现此接口,并使用SPI进行拓展.
 *
 * <pre>{@code
 *
 *  META-INF/services/org.hswebframework.web.exception.analyzer.ExceptionAnalyzer
 *
 * }</pre>
 *
 * @author zhouhao
 * @since 4.0.18
 * @see ExceptionAnalyzerReporter
 */
public interface ExceptionAnalyzer {

    /**
     * 执行分析
     *
     * @param error 异常信息
     * @return 是否被处理
     */
    boolean analyze(Throwable error);


}
