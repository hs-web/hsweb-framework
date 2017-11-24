package org.hswebframework.web.database.manager.meta.table;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author zhouhao
 */
public class ColumnMetadata implements Serializable {
    private static final long serialVersionUID = 2068679809718583039L;
    private String name;

    private String comment;

    private String dataType;
    //长度
    private int    length;

    //精度
    private int precision;

    //小数位数
    private int scale;

    private boolean notNull;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

}
