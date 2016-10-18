package org.hsweb.web.mybatis.plgins.pager;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.core.datasource.DataSourceHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Intercepts({@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
        RowBounds.class, ResultHandler.class})})
@Component
public class PagerInterceptor implements Interceptor {
    protected Map<String, PagerHelper> pagerHelperBase = new HashMap<>();

    @Autowired
    private ApplicationContext context;

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
            Object obj = statementHandler.getParameterHandler().getParameterObject();
            if (obj instanceof QueryParam) {
                QueryParam param = (QueryParam) obj;
                PagerHelper helper = pagerHelperBase.get(getDialect());
                if (helper != null && param.isPaging() && !sql.contains("count(0)")) {
                    String newSql = helper.doPaging(param, sql);
                    metaStatementHandler.setValue("delegate.boundSql.sql", newSql);
                }
            }
        }
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

    @PostConstruct
    public void init() {
        Map<String, PagerHelper> helperMap = context.getBeansOfType(PagerHelper.class);
        helperMap.forEach((name, helper) -> pagerHelperBase.put(helper.getDialect(), helper));
    }

    public String getDialect() {
        return DataSourceHolder.getDefaultDatabaseType().name();
    }
}