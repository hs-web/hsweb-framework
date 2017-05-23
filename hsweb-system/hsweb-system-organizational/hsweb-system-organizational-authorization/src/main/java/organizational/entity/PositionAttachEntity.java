package organizational.entity;

import java.io.Serializable;

/**
 * @author zhouhao
 */
public interface PositionAttachEntity extends Serializable {
    String positionId = "positionId";

    String getPositionId();

    void setPositionId(String positionId);
}
