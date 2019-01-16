/*
 *
 *  * Copyright 2019 http://www.hswebframework.org
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

package org.hswebframework.web.dao.mybatis.plgins.pager;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.web.dao.mybatis.builder.EasyOrmSqlBuilder;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Intercepts({@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
        RowBounds.class, ResultHandler.class})})
@Component
public class PagerInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation target) throws Throwable {
        return target.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            StatementHandler statementHandler = (StatementHandler) target;
            MetaObject metaStatementHandler = SystemMetaObject.forObject(statementHandler);
            String sql = statementHandler.getBoundSql().getSql();
            Pager pager = Pager.getAndReset();
            String newSql = sql;
            if (sql.trim().toLowerCase().startsWith("select")) {
                if (pager != null) {
                    newSql = EasyOrmSqlBuilder.getInstance()
                            .getActiveDatabase().getDialect()
                            .doPaging(sql, pager.pageIndex(), pager.pageSize());
                }
                Object queryEntity = statementHandler.getParameterHandler().getParameterObject();
                if (queryEntity instanceof QueryParam && ((QueryParam) queryEntity).isForUpdate()) {
                    newSql = newSql + " for update";
                }
                metaStatementHandler.setValue("delegate.boundSql.sql", newSql);
            }

        }
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}