package org.hswebframework.web.organizational.authorization.entity;

import org.hswebframework.web.commons.entity.Entity;

/**
 * @author zhouhao
 */
public interface OrgAttachEntity extends Entity {
    String orgId = "orgId";

    String getOrgId();

    void setOrgId(String orgId);
}
