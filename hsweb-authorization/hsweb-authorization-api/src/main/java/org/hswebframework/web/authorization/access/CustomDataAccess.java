package org.hswebframework.web.authorization.access;

/**
 * 自定义控制器的数据级权限控制器
 *
 * @author zhouhao
 * @see DataAccess.Type#CUSTOM
 */
public interface CustomDataAccess extends DataAccess {

    /**
     * @return 自定义的控制器
     */
    DataAccessController getController();

    default String getType() {
        return Type.CUSTOM.name();
    }
}
