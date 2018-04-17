package org.hswebframework.web.dictionary.api;

/**
 * 字典包装器,用于将实体类中的字段保存到数据库,或者将数据库中的字典信息包装到实体中
 *
 * @author zhouhao
 * @since 3.0
 */
public interface DictionaryWrapper {
    <T> T wrap(Object id, T bean);

    <T> T persistent(Object id, T bean);
}
