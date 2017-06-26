package org.hswebframework.web.service.dictionary.builder;

import org.hswebframework.web.service.dictionary.parser.SingleDictParser;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface DictionaryParserBuilder {
    SingleDictParser build(String config);
}
