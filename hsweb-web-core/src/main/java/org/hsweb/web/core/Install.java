package org.hsweb.web.core;

import org.hsweb.commons.file.FileUtils;
import org.hsweb.ezorm.rdb.executor.SqlExecutor;
import org.hsweb.ezorm.rdb.render.SqlAppender;
import org.hsweb.ezorm.rdb.render.support.simple.SimpleSQL;
import org.hsweb.web.core.datasource.DataSourceHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class Install {
    /**
     * 获取当前数据库类型
     *
     * @return
     */
    public static String getDatabaseType() {
        return DataSourceHolder.getActiveDatabaseType().name();
    }

    @Autowired
    private SqlExecutor sqlExecutor;

    @PostConstruct
    public void install() throws Exception {
        String dbType = DataSourceHolder.getActiveDatabaseType().name();
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

    protected void execInstallSql(InputStream sqlStream) throws Exception {
        String username = "";
        Connection connection = null;
        try {
            connection = DataSourceHolder.getActiveSource().getConnection();
            username = connection.getMetaData().getUserName();
        } finally {
            if (null != connection) connection.close();
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sqlStream, "utf-8"));
        List<String> sqlList = new ArrayList<>();
        SqlAppender tmp = new SqlAppender();
        String uname = username;
        bufferedReader.lines().forEach((line) -> {
            if (line.startsWith("--")) return;
            line = line.replace("${jdbc.username}", uname);
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
                throw new RuntimeException("install sql fail", e);
            }
        });
    }

}
