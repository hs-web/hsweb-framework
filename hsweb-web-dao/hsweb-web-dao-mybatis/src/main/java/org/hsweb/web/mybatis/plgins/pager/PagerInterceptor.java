package org.hsweb.web.mybatis.plgins.pager;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.core.datasource.DataSourceHolder;
import org.hsweb.web.mybatis.builder.EasyOrmSqlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            if (pager != null && sql.trim().toLowerCase().startsWith("select")) {
                String newSql = EasyOrmSqlBuilder.getInstance()
                        .getActiveDatabase().getDialect()
                        .doPaging(sql, pager.pageIndex(), pager.pageSize());
                metaStatementHandler.setValue("delegate.boundSql.sql", newSql);
            }
        }
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}