package org.hswebframework.web.entity.organizational.authorization;

import org.hswebframework.web.commons.entity.Entity;

/**
 * @author zhouhao
 */
public interface AreaAttachEntity extends Entity {
    String areaId = "areaId";

    String getAreaId();

    void setAreaId(String areaId);
}
