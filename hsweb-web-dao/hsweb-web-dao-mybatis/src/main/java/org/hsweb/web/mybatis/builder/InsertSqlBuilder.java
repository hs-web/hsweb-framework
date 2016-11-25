package org.hsweb.web.mybatis.builder;

import org.hsweb.ezorm.rdb.meta.RDBColumnMetaData;
import org.hsweb.ezorm.rdb.render.SqlAppender;
import org.hsweb.ezorm.rdb.render.support.simple.SimpleInsertSqlRender;

/**
 * @author zhouhao
 */
public class InsertSqlBuilder extends SimpleInsertSqlRender {
    @Override
    protected SqlAppender getParamString(String paramName, RDBColumnMetaData rdbColumnMetaData) {
        return new SqlAppender().add("#{", paramName,
                ",javaType=", EasyOrmSqlBuilder.getJavaType(rdbColumnMetaData.getJavaType()),
                ",jdbcType=", rdbColumnMetaData.getJdbcType(), "}");
    }
}
