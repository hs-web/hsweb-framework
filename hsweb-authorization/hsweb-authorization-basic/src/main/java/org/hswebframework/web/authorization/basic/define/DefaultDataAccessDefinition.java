package org.hswebframework.web.authorization.basic.define;

import org.hswebframework.web.authorization.define.DataAccessDefinition;

/**
 * @author zhouhao
 */
public class DefaultDataAccessDefinition implements DataAccessDefinition {

    private String controller;

    private String idParameterName="id";
    @Override
    public String getController() {
        return controller;
    }

    @Override
    public String getIdParameterName() {
        return idParameterName;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public void setIdParameterName(String idParameterName) {
        this.idParameterName = idParameterName;
    }
}
