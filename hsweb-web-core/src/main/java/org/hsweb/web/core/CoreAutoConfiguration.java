package org.hsweb.web.core;

import org.hsweb.commons.StringUtils;
import org.hsweb.expands.script.engine.DynamicScriptEngine;
import org.hsweb.expands.script.engine.DynamicScriptEngineFactory;
import org.hsweb.ezorm.rdb.executor.SqlExecutor;
import org.hsweb.ezorm.rdb.meta.RDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.meta.parser.H2TableMetaParser;
import org.hsweb.ezorm.rdb.meta.parser.MysqlTableMetaParser;
import org.hsweb.ezorm.rdb.meta.parser.OracleTableMetaParser;
import org.hsweb.ezorm.rdb.meta.parser.SqlServer2012TableMetaParser;
import org.hsweb.ezorm.rdb.render.dialect.H2RDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.render.dialect.MSSQLRDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.render.dialect.MysqlRDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.render.dialect.OracleRDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.simple.SimpleDatabase;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.authorize.ExpressionScopeBean;
import org.hsweb.web.core.datasource.DataSourceHolder;
import org.hsweb.web.core.datasource.DatabaseType;
import org.hsweb.web.core.utils.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Configuration
@ComponentScan("org.hsweb.web.core")
public class CoreAutoConfiguration {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    DataSource dataSource;
    @Autowired
    SqlExecutor sqlExecutor;
    @Autowired(required = false)
    private Map<String, ExpressionScopeBean> expressionScopeBeanMap = new HashMap<>();
    private SimpleDatabase database;
    private String initializeScript = "classpath*:scripts/startup/*.";

    @PostConstruct
    public void init() throws SQLException {
        initScriptVariable();
        initScript();
    }

    /**
     * 初始化脚本参数.
     *
     * @throws SQLException
     */
    private void initScriptVariable() throws SQLException {
        DatabaseType type = DataSourceHolder.getDefaultDatabaseType();
        Connection connection = null;
        String jdbcUserName;
        try {
            connection = DataSourceHolder.getActiveSource().getConnection();
            jdbcUserName = connection.getMetaData().getUserName();
        } finally {
            if (null != connection) connection.close();
        }
        RDBDatabaseMetaData metaData;
        switch (type) {
            case oracle:
                metaData = new OracleRDBDatabaseMetaData();
                metaData.setParser(new OracleTableMetaParser(sqlExecutor));
                break;
            case mysql:
                metaData = new MysqlRDBDatabaseMetaData();
                metaData.setParser(new MysqlTableMetaParser(sqlExecutor));
                break;
            case jtds_sqlserver:
            case sqlserver:
                metaData = new MSSQLRDBDatabaseMetaData();
                metaData.setParser(new SqlServer2012TableMetaParser(sqlExecutor));
                break;
            default:
                h2:
                metaData = new H2RDBDatabaseMetaData();
                metaData.setParser(new H2TableMetaParser(sqlExecutor));
                break;
        }
        this.database = new SimpleDatabase(metaData, sqlExecutor);
        this.database.setAutoParse(true);
    }

    private void initScript() {
        Map<String, Object> vars = new HashMap<>(expressionScopeBeanMap);
        vars.put("LoginUser", (Supplier<User>) WebUtil::getLoginUser);
        vars.put("StringUtils", StringUtils.class);
        vars.put("User", User.class);

        initScript("js", vars);
        initScript("groovy", vars);
        initScript("java", vars);
        initScript("spel", vars);
        initScript("ognl", vars);
        initScript("ruby", vars);
        initScript("python", vars);
        //执行脚本
    }

    private void initScript(String language, Map<String, Object> vars) {
        try {
            DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(language);
            if (engine == null) return;
            vars.put("logger", LoggerFactory.getLogger("org.hsweb.script.".concat(language)));
            vars.put("scriptEngine", engine);
            engine.addGlobalVariable(vars);
            Map<String, Object> scriptVars = new HashMap<>();
            scriptVars.put("database", database);
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(initializeScript.concat(language));
            for (Resource resource : resources) {
                String script = StreamUtils.copyToString(resource.getInputStream(), Charset.forName("utf-8"));
                engine.compile("__tmp", script);
                try {
                    engine.execute("__tmp", scriptVars);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                } finally {
                    engine.remove("__tmp");
                }
            }
        } catch (NullPointerException e) {
            //
        } catch (IOException e) {
            logger.error("读取脚本文件失败", e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
