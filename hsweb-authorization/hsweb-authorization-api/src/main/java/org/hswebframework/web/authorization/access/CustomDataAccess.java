package org.hswebframework.web.authorization.access;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 * @see DataAccess.Type#CUSTOM
 */
public interface CustomDataAccess extends DataAccess {
    DataAccessController getController();
}
