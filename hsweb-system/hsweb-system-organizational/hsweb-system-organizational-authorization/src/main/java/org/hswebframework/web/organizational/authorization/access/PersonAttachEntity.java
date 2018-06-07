package org.hswebframework.web.organizational.authorization.access;

import org.hswebframework.web.commons.entity.Entity;

/**
 * 关联人员信息实体,实现该接口,表示实体与机构进行关联,在进行权限控制时,将会使用到该接口
 * @author zhouhao
 */
public interface PersonAttachEntity  extends Entity {
    String personId = "personId";

    String getPersonId();

    void setPersonId(String personId);
}
