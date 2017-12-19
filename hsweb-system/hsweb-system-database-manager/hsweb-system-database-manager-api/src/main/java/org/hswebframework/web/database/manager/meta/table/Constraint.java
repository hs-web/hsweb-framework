package org.hswebframework.web.database.manager.meta.table;

import lombok.Data;

import java.io.Serializable;

/**
 * 约束
 *
 * @author zhouhao
 * @since 3.0
 */
@Data
public class Constraint implements Serializable {
    private static final long serialVersionUID = 6594361915290310179L;

    /**
     * 表名
     *
     * @see TableMetadata#getName()
     */
    private String table;

    /**
     * 列名
     *
     * @see ColumnMetadata#getName()
     */
    private String column;

    /**
     * 约束类型
     */
    private Type type;

    public enum Type {
        /**
         * 主键
         */
        PrimaryKey,
        /**
         * 外键
         */
        ForeignKey,
        /**
         * 唯一约束
         */
        Unique, Check, Default
    }
}
