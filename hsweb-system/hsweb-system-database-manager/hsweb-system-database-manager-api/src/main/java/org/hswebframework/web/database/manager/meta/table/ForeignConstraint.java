package org.hswebframework.web.database.manager.meta.table;

/**
 * @author zhouhao
 */
public class ForeignConstraint extends Constraint {
    private static final long serialVersionUID = -7146549641064694467L;
    private String targetTable;

    private String targetColumn;

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public String getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(String targetColumn) {
        this.targetColumn = targetColumn;
    }
}
