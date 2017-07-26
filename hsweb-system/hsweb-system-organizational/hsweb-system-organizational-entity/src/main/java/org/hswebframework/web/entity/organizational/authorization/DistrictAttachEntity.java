package org.hswebframework.web.entity.organizational.authorization;

import org.hswebframework.web.commons.entity.Entity;

/**
 * @author zhouhao
 */
public interface DistrictAttachEntity extends Entity {
    String districtId = "districtId";

    String getDistrictId();

    void setDistrictId(String districtId);
}
