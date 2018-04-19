package org.hswebframework.web.dictionary.simple.parser;

import java.util.List;
import java.util.function.BiFunction;

/**
 * @author zhouhao
 * @see 3.0
 */
public interface DictParserFormatter {
    <T> List<FormatterResult<T>> format(DictParserFormat format, Object value, BiFunction<String, String, T> mapping);
}
