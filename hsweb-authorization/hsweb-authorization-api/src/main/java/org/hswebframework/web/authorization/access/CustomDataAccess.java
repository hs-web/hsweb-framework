package org.hswebframework.web.authorization.access;

/**
 * @author zhouhao
 * @see DataAccess.Type#CUSTOM
 */
public interface CustomDataAccess extends DataAccess {
    DataAccessController getController();

    default String getType() {
        return Type.CUSTOM.name();
    }
}
