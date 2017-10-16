package org.hswebframework.web.authorization.access;

/**
 * 只能操作由自己创建的数据
 *
 * @author zhouhao
 */
public interface OwnCreatedDataAccessConfig extends DataAccessConfig {
    @Override
    default String getType() {
        return DefaultType.OWN_CREATED;
    }
}
