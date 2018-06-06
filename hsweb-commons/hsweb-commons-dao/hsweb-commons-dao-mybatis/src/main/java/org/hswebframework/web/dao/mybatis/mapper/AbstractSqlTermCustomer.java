package org.hswebframework.web.dao.mybatis.mapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.dialect.Dialect;

import java.util.List;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@AllArgsConstructor
public abstract class AbstractSqlTermCustomer implements SqlTermCustomer {

    @Getter
    protected final String termType;

    @Override
    public Dialect[] forDialect() {
        return null;
    }

    protected String createColumnName(RDBColumnMetaData column, String tableAlias) {
        return column.getTableMetaData().getDatabaseMetaData().getDialect().buildColumnName(tableAlias, column.getName());
    }

    protected void appendCondition(List<Object> values, String wherePrefix, SqlAppender appender) {
        int len = values.size();
        if (len == 1) {
            appender.add("=#{", wherePrefix, ".value[0]}");
        } else {
            appender.add("in(");
            for (int i = 0; i < len; i++) {
                if (i > 0) {
                    appender.add(",");
                }
                appender.add("#{", wherePrefix, ".value[" + i + "]}");
            }
            appender.add(")");
        }

    }
}
