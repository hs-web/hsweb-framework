package org.hsweb.web.mybatis.builder;

import org.hsweb.ezorm.core.param.UpdateParam;
import org.hsweb.ezorm.rdb.executor.SQL;
import org.hsweb.ezorm.rdb.meta.RDBColumnMetaData;
import org.hsweb.ezorm.rdb.meta.RDBTableMetaData;
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
    public SQL render(RDBTableMetaData metaData, UpdateParam param) {
        RDBTableMetaData metaDataNew = metaData.clone();
        metaDataNew.setDatabaseMetaData(metaData.getDatabaseMetaData());

        metaDataNew.getColumns().stream()
                .filter(column -> column.getName().contains("."))
                .map(RDBColumnMetaData::getName)
                .forEach(metaDataNew::removeColumn);
        return super.render(metaDataNew, param);
    }
    @Override
    protected SqlAppender getParamString(String paramName, RDBColumnMetaData rdbColumnMetaData) {
        return new SqlAppender().add("#{", paramName,
                ",javaType=", EasyOrmSqlBuilder.getJavaType(rdbColumnMetaData.getJavaType()),
                ",jdbcType=", rdbColumnMetaData.getJdbcType(), "}");
    }
}
