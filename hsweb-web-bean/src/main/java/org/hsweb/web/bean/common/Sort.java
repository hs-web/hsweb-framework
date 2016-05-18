package org.hsweb.web.bean.common;

/**
 * Created by zhouhao on 16-5-14.
 */
public class Sort {
    private String field;

    private String dir = "asc";

    private transient QueryParam queryParam;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public Sort() {
    }

    public Sort(QueryParam queryParam, String field) {
        this.queryParam = queryParam;
        this.field = field;
    }

    public Sort(String field) {
        this.field = field;
    }

    public QueryParam asc() {
        this.dir = "asc";
        return queryParam;
    }

    public QueryParam desc() {
        this.dir = "desc";
        return queryParam;
    }

    public Sort and(String field) {
        return queryParam.orderBy(field);
    }

    @Override
    public int hashCode() {
        return String.valueOf(field).concat(dir).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        return this.hashCode() == obj.hashCode();
    }
}
