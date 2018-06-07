package org.hswebframework.web.organizational.authorization.access;

import org.hswebframework.web.commons.entity.Entity;

/**
 * 关联机构信息实体,实现该接口,表示实体与机构进行关联,在进行权限控制时,将会使用到该接口
 *
 * @author zhouhao
 * @since 3.0
 */
public interface OrgAttachEntity extends Entity {
    String orgId = "orgId";

    String getOrgId();

    void setOrgId(String orgId);
}
