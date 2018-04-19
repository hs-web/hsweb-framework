package org.hswebframework.web.dictionary.api;

/**
 * 字典包装器,用于将实体类中的字段保存到数据库,或者将数据库中的字典信息包装到实体中
 *
 * @author zhouhao
 * @since 3.0
 */
public interface DictionaryWrapper {
    /**
     * 向一个实体类里填充数据字典
     *
     * @param id   实体类的id
     * @param bean 实体类
     * @param <T>  实体类型
     * @return 填充后的实体类
     */
    <T> T wrap(Object id, T bean);

    /**
     * 把实体类中的
     * @param id
     * @param bean
     * @param <T>
     * @return
     */
    <T> T persistent(Object id, T bean);
}
