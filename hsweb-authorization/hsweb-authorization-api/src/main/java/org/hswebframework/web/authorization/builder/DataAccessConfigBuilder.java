package org.hswebframework.web.authorization.builder;

import org.hswebframework.web.authorization.access.DataAccessConfig;

import java.util.Map;

/**
 *
 * @author zhouhao
 */
public interface DataAccessConfigBuilder {
    DataAccessConfigBuilder fromJson(String json);

    DataAccessConfigBuilder fromMap(Map<String,Object> json);

    DataAccessConfig build();
}
