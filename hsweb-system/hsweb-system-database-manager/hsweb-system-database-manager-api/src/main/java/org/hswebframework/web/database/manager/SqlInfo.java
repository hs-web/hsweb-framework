package org.hswebframework.web.database.manager;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhouhao
 */
@Data
public class SqlInfo implements Serializable {
    private static final long serialVersionUID = -2119739552930123239L;
    private String sql;

    private String datasourceId;

    private String type;
}
