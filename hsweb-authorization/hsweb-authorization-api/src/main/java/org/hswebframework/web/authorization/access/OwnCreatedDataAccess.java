package org.hswebframework.web.authorization.access;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface OwnCreatedDataAccess extends DataAccess {
    default String getType() {
        return Type.OWN_CREATED.name();
    }
}
