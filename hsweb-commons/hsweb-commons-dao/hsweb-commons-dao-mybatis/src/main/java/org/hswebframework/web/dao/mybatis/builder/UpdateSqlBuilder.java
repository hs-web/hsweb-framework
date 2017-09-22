/*
 *
 *  * Copyright 2016 http://www.hswebframework.org
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.hswebframework.web.dao.mybatis.builder;

import org.hswebframework.ezorm.core.param.UpdateParam;
import org.hswebframework.ezorm.rdb.executor.SQL;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.meta.RDBTableMetaData;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.dialect.Dialect;
import org.hswebframework.ezorm.rdb.render.support.simple.SimpleUpdateSqlRender;

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
