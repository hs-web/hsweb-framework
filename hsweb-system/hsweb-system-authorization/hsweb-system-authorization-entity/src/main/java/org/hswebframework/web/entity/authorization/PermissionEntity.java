package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.GenericEntity;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface PermissionEntity extends GenericEntity<String> {

    String getId();

    String getName();

    String getDescribe();

    Byte getStatus();

    void setName(String name);

    void setDescribe(String comment);

    void setStatus(Byte status);

    List<ActionEntity> getActions();

    void setActions(List<ActionEntity> actions);

    List<DataAccessEntity> getDataAccess();

    List<FieldAccessEntity> getFieldAccess();

    void setDataAccess(List<DataAccessEntity> dataAccess);

    void setFieldAccess(List<FieldAccessEntity> fieldAccess);
}
