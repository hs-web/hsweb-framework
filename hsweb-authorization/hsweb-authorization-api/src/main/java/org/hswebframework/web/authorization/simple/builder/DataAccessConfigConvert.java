package org.hswebframework.web.authorization.simple.builder;

import org.hswebframework.web.authorization.access.DataAccessConfig;

/**
 * @author zhouhao
 */
public interface DataAccessConfigConvert {

    boolean isSupport(String type, String action, String config);

    DataAccessConfig convert(String type, String action, String config);
}
