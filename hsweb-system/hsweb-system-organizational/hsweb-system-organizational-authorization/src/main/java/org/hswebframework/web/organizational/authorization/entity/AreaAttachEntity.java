package org.hswebframework.web.organizational.authorization.entity;

import org.hswebframework.web.commons.entity.Entity;

/**
 * @author zhouhao
 */
public interface AreaAttachEntity extends Entity {
    String areaId = "areaId";

    String getAreaId();

    void setAreaId(String areaId);
}
