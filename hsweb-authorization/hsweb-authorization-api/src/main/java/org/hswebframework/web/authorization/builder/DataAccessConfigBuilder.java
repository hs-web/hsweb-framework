package org.hswebframework.web.authorization.builder;

import org.hswebframework.web.authorization.access.DataAccessConfig;

/**
 *
 * @author zhouhao
 */
public interface DataAccessConfigBuilder {
    DataAccessConfigBuilder fromJson(String json);

    DataAccessConfig build();
}
