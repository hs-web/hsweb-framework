package org.hswebframework.web.authorization.builder;

import org.hswebframework.web.authorization.access.FieldAccessConfig;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface FieldAccessConfigBuilder {
    FieldAccessConfigBuilder fromJson(String json);

    FieldAccessConfig build();
}
