package org.hsweb.web.mybatis.builder;

import org.hsweb.ezorm.core.param.InsertParam;
import org.hsweb.ezorm.rdb.executor.SQL;
import org.hsweb.ezorm.rdb.meta.RDBColumnMetaData;
import org.hsweb.ezorm.rdb.meta.RDBTableMetaData;
import org.hsweb.ezorm.rdb.render.SqlAppender;
import org.hsweb.ezorm.rdb.render.support.simple.SimpleInsertSqlRender;

/**
 * @author zhouhao
 */
public class InsertSqlBuilder extends SimpleInsertSqlRender {
    @Override
    public SQL render(RDBTableMetaData metaData, InsertParam param) {
        RDBTableMetaData metaDataNew = metaData.clone();
        metaDataNew.setDatabaseMetaData(metaData.getDatabaseMetaData());
        metaDataNew.getColumns().stream()
                .filter(column -> column.getName().contains("."))
                .map(RDBColumnMetaData::getName)
                .forEach(metaDataNew::removeColumn);
        return super.render(metaDataNew, param);
    }

    @Override
    protected SqlAppender getParamString(String prefix, String paramName, RDBColumnMetaData rdbColumnMetaData) {
        return new SqlAppender().add("#{", prefix, paramName,
                ",javaType=", EasyOrmSqlBuilder.getJavaType(rdbColumnMetaData.getJavaType()),
                ",jdbcType=", rdbColumnMetaData.getJdbcType(), "}");
    }
}
