package org.hsweb.web.core;

import org.hsweb.commons.file.FileUtils;
import org.hsweb.ezorm.executor.SqlExecutor;
import org.hsweb.ezorm.render.SqlAppender;
import org.hsweb.ezorm.render.support.simple.SimpleSQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouhao on 16-4-23.
 */
@Configuration
@EnableConfigurationProperties({DataSourceProperties.class})
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
@ConfigurationProperties(
        prefix = "spring.datasource"
)
public class Install {
    private static String DATABASE_TYPE = "h2";

    /**
     * 获取当前数据库类型
     *
     * @return
     */
    public static String getDatabaseType() {
        return DATABASE_TYPE;
    }

    @Autowired
    private DataSourceProperties properties;

    @Autowired
    private SqlExecutor sqlExecutor;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void install() throws Exception {
        String dc = properties.getDriverClassName();
        String dbType = dc.contains("mysql") ? "mysql" : dc.contains("oracle") ? "oracle" : dc.contains("h2") ? "h2" : null;
        DATABASE_TYPE = dbType;
        Assert.notNull(dbType, "不支持的数据库类型");
        try {
            boolean firstInstall = false;
            try {
                sqlExecutor.exec(new SimpleSQL("select * from s_user where 1=2"));
            } catch (Exception e) {
                firstInstall = true;
            }
            if (firstInstall) {
                //表结构
                InputStream reader = FileUtils.getResourceAsStream("system/install/sql/" + dbType + "/install.sql");
                execInstallSql(reader);
                String installSqlName = "classpath*:/system/install/sql/" + dbType + "/*-data.sql";
                Resource[] resources = new PathMatchingResourcePatternResolver().getResources(installSqlName);
                for (Resource resource : resources) {
                    if (resource.isReadable()) {
                        execInstallSql(resource.getInputStream());
                    }
                }
            }
        } catch (IOException e) {
            throw e;
        }
    }

    protected void execInstallSql(InputStream sqlStream) throws UnsupportedEncodingException {
        String username = properties.getUsername();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sqlStream,"utf-8"));
        List<String> sqlList = new ArrayList<>();
        SqlAppender tmp = new SqlAppender();
        bufferedReader.lines().forEach((line) -> {
            if (line.startsWith("--")) return;
            line = line.replace("${jdbc.username}", username);
            //去除sql中的;
            if (line.endsWith(";"))
                tmp.add(line.substring(0, line.length() - 1));
            else
                tmp.add(line);
            tmp.add("\n");
            if (line.endsWith(";")) {
                sqlList.add(tmp.toString());
                tmp.clear();
            }
        });
        sqlList.forEach((sql) -> {
            try {
                sqlExecutor.exec(new SimpleSQL(sql));
            } catch (Exception e) {
                logger.warn("install sql fail", e);
            }
        });
    }

}
