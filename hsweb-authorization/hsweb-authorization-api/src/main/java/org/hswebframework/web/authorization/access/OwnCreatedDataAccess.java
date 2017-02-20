package org.hswebframework.web.authorization.access;

/**
 * 只能操作由自己创建的数据
 *
 * @author zhouhao
 */
public interface OwnCreatedDataAccess extends DataAccess {
    default String getType() {
        return Type.OWN_CREATED.name();
    }
}
