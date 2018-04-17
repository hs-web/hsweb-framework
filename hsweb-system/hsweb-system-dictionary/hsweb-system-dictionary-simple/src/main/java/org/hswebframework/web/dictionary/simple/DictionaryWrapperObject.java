package org.hswebframework.web.dictionary.simple;

/**
 * @author zhouhao
 * @since 3.0
 */
public interface DictionaryWrapperObject {

    void wrap(Object id, Object bean, DefaultDictionaryHelper helper);

    void persistent(Object id, Object bean, DefaultDictionaryHelper helper);

}
