package organizational.entity;

import java.io.Serializable;

/**
 * @author zhouhao
 */
public interface PersonAttachEntity extends Serializable {
    String personId = "personId";

    String getPersonId();

    void setPersonId(String personId);
}
