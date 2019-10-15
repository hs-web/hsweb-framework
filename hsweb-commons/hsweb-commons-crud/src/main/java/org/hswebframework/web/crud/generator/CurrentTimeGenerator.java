package org.hswebframework.web.crud.generator;

import org.hswebframework.ezorm.core.DefaultValue;
import org.hswebframework.ezorm.core.DefaultValueGenerator;
import org.hswebframework.ezorm.core.RuntimeDefaultValue;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;

import java.time.LocalDateTime;
import java.util.Date;

public class CurrentTimeGenerator implements DefaultValueGenerator<RDBColumnMetadata> {
    @Override
    public String getSortId() {
        return Generators.CURRENT_TIME;
    }

    @Override
    public DefaultValue generate(RDBColumnMetadata metadata) {
        return (RuntimeDefaultValue) () -> generic(metadata.getJavaType());
    }

    protected Object generic(Class type) {
        if (type == Date.class) {
            return new Date();
        }
        if (type == java.sql.Date.class) {
            return new java.sql.Date(System.currentTimeMillis());
        }
        if (type == LocalDateTime.class) {
            return LocalDateTime.now();
        }
        return System.currentTimeMillis();
    }

    @Override
    public String getName() {
        return "当前系统时间";
    }
}
