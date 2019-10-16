package org.hswebframework.web.authorization.access;

/**
 * 只能操作由自己创建的数据
 *
 * @author zhouhao
 */
public interface OwnCreatedDataAccessConfig extends DataAccessConfig {
    @Override
    default DataAccessType getType() {
        return DefaultDataAccessType.USER_OWN_DATA;
    }
}
