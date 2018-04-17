package org.hswebframework.web.dictionary.api.builder;


import org.hswebframework.web.dictionary.api.parser.SingleDictParser;

/**
 * @author zhouhao
 */
public interface DictionaryParserBuilder {
    SingleDictParser build(String config);
}
