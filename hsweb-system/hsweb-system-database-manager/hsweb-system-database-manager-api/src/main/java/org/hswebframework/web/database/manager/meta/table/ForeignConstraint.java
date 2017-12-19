package org.hswebframework.web.database.manager.meta.table;

import lombok.Data;

/**
 * @author zhouhao
 */
@Data
public class ForeignConstraint extends Constraint {
    private static final long serialVersionUID = -7146549641064694467L;
    private String targetTable;

    private String targetColumn;

}
