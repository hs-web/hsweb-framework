package organizational.entity;

import java.io.Serializable;

/**
 * @author zhouhao
 */
public interface OrgAttachEntity extends Serializable {
    String orgId = "orgId";

    String getOrgId();

    void setOrgId(String orgId);
}
