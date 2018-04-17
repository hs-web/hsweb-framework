package org.hswebframework.web.dict.apply;

/**
 * 数据字典应用类
 *
 * @author zhouhao
 * @since 3.0
 */
public interface DictApply {

    <T> T apply(T bean);

}
