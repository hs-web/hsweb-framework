package org.hswebframework.web.dictionary.simple;


/**
 * @author zhouhao
 * @since 3.0
 */
public interface DefaultDictionaryHelper {
    Object getDictEnum(Object id, String targetKey, String dictId, Class type);

    void persistent(Object id, String targetKey, String dictId, Class type,Object value);
}
