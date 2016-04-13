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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

//@Intercepts({ @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
//        RowBounds.class, ResultHandler.class }) })
//@Component
//@ConfigurationProperties(
//        prefix = "spring.datasource"
//)
public class PagerInterceptor implements Interceptor {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected Map<String, PagerHelper> pagerHelperBase = new HashMap<>();

    protected String dialect = null;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private DataSourceProperties properties;

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
                PagerHelper helper = pagerHelperBase.get(dialect);
                if (helper != null) {
                    String newSql = helper.doPaging(param, sql);
                    metaStatementHandler.setValue("delegate.boundSql.sql", newSql);
                }
            }
        }
        return Plugin.wrap(target,this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    @PostConstruct
    public void init() {
        Map<String, PagerHelper> helperMap = context.getBeansOfType(PagerHelper.class);
        helperMap.forEach((name, helper) -> pagerHelperBase.put(helper.getDialect(), helper));
        dialect = getDialect();
    }

    public String getDialect() {
        String url = properties.getDriverClassName();
        if (url.contains("mysql")) {
            return "mysql";
        }
        if (url.contains("oracle")) {
            return "oracle";
        }
        logger.error("mybaits pager dialect not found!");
        return "undefine";
    }
}