package org.hswebframework.web.database.manager.meta.table;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class ForeignConstraint extends Constraint {
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
