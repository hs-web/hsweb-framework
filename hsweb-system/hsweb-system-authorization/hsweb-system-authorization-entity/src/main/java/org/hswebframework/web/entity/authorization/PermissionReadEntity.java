package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.Entity;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface PermissionReadEntity<A extends ActionEntity> extends Entity{
    String getId();

    String getName();

    String getDescribe();

    byte getStatus();

    List<A> getActions();
}
