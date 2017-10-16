package org.hswebframework.web.authorization.access;

/**
 * 自定义控制器的数据级权限控制器
 *
 * @author zhouhao
 * @see DefaultType#CUSTOM
 */
public interface CustomDataAccessConfig extends DataAccessConfig {

    /**
     * @return 自定义的控制器
     */
    DataAccessController getController();

    @Override
    default String getType() {
        return DefaultType.CUSTOM;
    }
}
