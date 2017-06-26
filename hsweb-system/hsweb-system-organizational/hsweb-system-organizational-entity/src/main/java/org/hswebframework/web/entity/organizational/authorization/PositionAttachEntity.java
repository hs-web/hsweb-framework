package org.hswebframework.web.entity.organizational.authorization;

import org.hswebframework.web.commons.entity.Entity;

/**
 * @author zhouhao
 */
public interface PositionAttachEntity extends Entity {
    String positionId = "positionId";

    String getPositionId();

    void setPositionId(String positionId);
}
