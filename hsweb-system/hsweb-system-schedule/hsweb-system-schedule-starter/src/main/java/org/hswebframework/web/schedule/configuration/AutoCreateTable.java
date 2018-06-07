package org.hswebframework.web.schedule.configuration;

import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.utils.file.FileUtils;
import org.hswebframework.web.Sqls;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.datasource.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

/**
 * @author zhouhao
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AutoCreateTable implements CommandLineRunner {

    @Autowired
    private SqlExecutor sqlExecutor;

    @Override
    public void run(String... args) throws Exception {
        if (sqlExecutor.tableExists("QRTZ_LOCKS")) {
            return;
        }
        DatabaseType databaseType = DataSourceHolder.currentDatabaseType();
        String databaseTypeName = databaseType.name();
        if (databaseType == DatabaseType.jtds_sqlserver) {
            databaseTypeName = DatabaseType.sqlserver.name();
        }
        String file = "classpath*:/quartz/sql/quartz-" + databaseTypeName + "-create.sql";
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(file);

        for (Resource resource : resources) {
            try (Reader reader = new InputStreamReader(resource.getInputStream())) {
                String str = FileUtils.reader2String(reader);
                List<String> sqlList = Sqls.parse(str);
                for (String sql : sqlList) {
                    if (StringUtils.isEmpty(sql)) return;
                    sqlExecutor.exec(sql);
                }
            }
        }
    }
}
