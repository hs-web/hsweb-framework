package org.hswebframework.web.organizational.authorization.entity;

import org.hswebframework.web.commons.entity.Entity;

/**
 * @author zhouhao
 */
public interface PositionAttachEntity extends Entity {
    String positionId = "positionId";

    String getPositionId();

    void setPositionId(String positionId);
}
