package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.access.OwnCreatedDataAccessConfig;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleOwnCreatedDataAccess extends AbstractDataAccess implements OwnCreatedDataAccessConfig {

    public SimpleOwnCreatedDataAccess() {
    }

    public SimpleOwnCreatedDataAccess(String action) {
        setAction(action);
    }
}
