package org.hswebframework.web.service.dictionary.simple.parser;

import java.util.List;
import java.util.function.BiFunction;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface DictParserFormatter {
    <T> List<FormatterResult<T>> format(DictParserFormat format, Object value, BiFunction<String, String, T> mapping);
}
