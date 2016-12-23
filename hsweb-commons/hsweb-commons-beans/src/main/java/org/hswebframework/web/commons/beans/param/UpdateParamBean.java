package org.hswebframework.web.commons.beans.param;

import org.hsweb.ezorm.core.param.UpdateParam;
import org.hswebframework.web.commons.beans.Bean;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class UpdateParamBean<T> extends UpdateParam<T> implements Bean {
    public UpdateParamBean() {
    }

    public UpdateParamBean(T data) {
        super(data);
    }
}
