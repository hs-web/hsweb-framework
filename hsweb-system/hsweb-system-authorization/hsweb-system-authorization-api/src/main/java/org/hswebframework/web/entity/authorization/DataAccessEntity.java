package org.hswebframework.web.entity.authorization;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.CloneableEntity;

/**
 * @author zhouhao
 */
@Getter
@Setter
@EqualsAndHashCode
public class DataAccessEntity implements CloneableEntity {
    private static final long serialVersionUID = 2198771924746804915L;

    private String action;

    private String describe;

    private String type;

    private String config;

    @Override
    public DataAccessEntity clone() {
        DataAccessEntity target = new DataAccessEntity();
        target.setDescribe(getDescribe());
        target.setAction(getAction());
        target.setConfig(getConfig());
        target.setType(getType());
        return target;
    }
}
