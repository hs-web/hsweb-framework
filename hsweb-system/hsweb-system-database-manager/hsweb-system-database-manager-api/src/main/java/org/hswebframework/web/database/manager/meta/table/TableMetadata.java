package org.hswebframework.web.database.manager.meta.table;

import org.hswebframework.web.database.manager.meta.ObjectMetadata;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class TableMetadata extends ObjectMetadata {
    private String comment;

    private List<Constraint> constraints;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
    }
}
