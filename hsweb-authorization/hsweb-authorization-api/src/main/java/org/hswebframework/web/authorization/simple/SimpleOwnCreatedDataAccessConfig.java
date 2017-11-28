package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.access.OwnCreatedDataAccessConfig;

/**
 * @author zhouhao
 * @since 3.0
 */
public class SimpleOwnCreatedDataAccessConfig extends AbstractDataAccessConfig implements OwnCreatedDataAccessConfig {

    private static final long serialVersionUID = -6059330812806119730L;

    public SimpleOwnCreatedDataAccessConfig() {
    }

    public SimpleOwnCreatedDataAccessConfig(String action) {
        setAction(action);
    }
}
