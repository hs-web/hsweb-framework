package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.GenericEntity;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface PermissionEntity<A extends ActionEntity> extends GenericEntity<String>,
        PermissionReadEntity<A>{
    void setName(String name);

    void setDescribe(String comment);

    void setStatus(byte status);

    void setActions(List<A> actions);

}
