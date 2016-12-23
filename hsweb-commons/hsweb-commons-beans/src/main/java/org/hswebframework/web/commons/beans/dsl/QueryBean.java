package org.hswebframework.web.commons.beans.dsl;

import org.hswebframework.web.commons.beans.Bean;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface QueryBean<T extends QueryBean> extends Bean {


    T like(Object value);

    T gt(Object value);


}
