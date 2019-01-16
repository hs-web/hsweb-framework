package org.hswebframework.web.organizational.authorization.access;

import org.hswebframework.web.commons.entity.Entity;

/**
 * @author zhouhao
 */
public interface DistrictAttachEntity extends Entity {
    String districtId = "districtId";

    String getDistrictId();

    void setDistrictId(String districtId);

    default String getDistrictIdProperty() {
        return districtId;
    }
}
