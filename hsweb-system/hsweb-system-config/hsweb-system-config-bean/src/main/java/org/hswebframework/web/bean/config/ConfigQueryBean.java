package org.hswebframework.web.bean.config;

import org.hsweb.ezorm.core.dsl.ConditionalBuilder;
import org.hswebframework.web.commons.beans.Bean;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface ConfigQueryBean extends Bean {
    ConditionalBuilder name = ConditionalBuilder.build("name");


}
