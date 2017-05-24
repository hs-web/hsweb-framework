package org.hswebframework.web.organizational.authorization.entity;

import org.hswebframework.web.commons.entity.Entity;

/**
 * @author zhouhao
 */
public interface PersonAttachEntity  extends Entity {
    String personId = "personId";

    String getPersonId();

    void setPersonId(String personId);
}
