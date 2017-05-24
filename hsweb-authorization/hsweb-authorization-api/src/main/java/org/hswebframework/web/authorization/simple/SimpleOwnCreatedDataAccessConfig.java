package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.access.OwnCreatedDataAccessConfig;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleOwnCreatedDataAccessConfig extends AbstractDataAccessConfig implements OwnCreatedDataAccessConfig {

    public SimpleOwnCreatedDataAccessConfig() {
    }

    public SimpleOwnCreatedDataAccessConfig(String action) {
        setAction(action);
    }
}
