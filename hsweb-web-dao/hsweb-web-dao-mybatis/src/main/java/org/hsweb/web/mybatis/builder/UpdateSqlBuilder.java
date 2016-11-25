package org.hsweb.web.mybatis.builder;

import org.hsweb.ezorm.rdb.meta.RDBColumnMetaData;
import org.hsweb.ezorm.rdb.render.SqlAppender;
import org.hsweb.ezorm.rdb.render.dialect.Dialect;
import org.hsweb.ezorm.rdb.render.support.simple.SimpleUpdateSqlRender;

/**
 * @author zhouhao
 */
public class UpdateSqlBuilder extends SimpleUpdateSqlRender {
    public UpdateSqlBuilder(Dialect dialect) {
        super(dialect);
    }

    @Override
    protected SqlAppender getParamString(String paramName, RDBColumnMetaData rdbColumnMetaData) {
        return new SqlAppender().add("#{", paramName,
                ",javaType=", EasyOrmSqlBuilder.getJavaType(rdbColumnMetaData.getJavaType()),
                ",jdbcType=", rdbColumnMetaData.getJdbcType(), "}");
    }
}
