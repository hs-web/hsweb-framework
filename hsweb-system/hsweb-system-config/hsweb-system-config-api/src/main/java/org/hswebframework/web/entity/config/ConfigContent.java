package org.hswebframework.web.entity.config;

import org.hswebframework.web.commons.entity.CloneableEntity;

import java.math.BigDecimal;

public class ConfigContent implements CloneableEntity {
    private String key;

    private Object value;

    private String comment;

    public ConfigContent() {
    }

    public ConfigContent(String key, Object value, String comment) {
        this.key = key;
        this.value = value;
        this.comment = comment;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Number getNumber(Number defaultValue) {
        Object val = getValue();
        if (null == val) {
            return defaultValue;
        }
        if (val instanceof Number) {
            return ((Number) val);
        }
        if (val instanceof String) {
            return new BigDecimal(((String) val));
        }
        return defaultValue;
    }

    public Object getValue(Object defaultValue) {
        Object val = getValue();
        if (val == null) {
            return defaultValue;
        }
        return val;
    }

    @Override
    public ConfigContent clone() {
        Object val = value;
        if (val instanceof CloneableEntity) {
            val = ((CloneableEntity) val).clone();
        }
        return new ConfigContent(key, val, comment);
    }
}