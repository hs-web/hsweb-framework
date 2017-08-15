package org.hswebframework.web.authorization.define;


import java.io.Serializable;

/**
 *
 * @author zhouhao
 * @see org.hswebframework.web.authorization.annotation.RequiresDataAccess
 */
public interface DataAccessDefinition extends Serializable {

    String getController();

    String getIdParameterName();

}
