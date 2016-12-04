package org.hsweb.web.starter;

import org.hsweb.commons.StringUtils;
import org.hsweb.expands.script.engine.DynamicScriptEngine;
import org.hsweb.expands.script.engine.DynamicScriptEngineFactory;
import org.hsweb.expands.script.engine.ExecuteResult;
import org.hsweb.ezorm.rdb.RDBDatabase;
import org.hsweb.ezorm.rdb.RDBTable;
import org.hsweb.ezorm.rdb.executor.SqlExecutor;
import org.hsweb.ezorm.rdb.meta.converter.ClobValueConverter;
import org.hsweb.ezorm.rdb.meta.converter.JSONValueConverter;
import org.hsweb.ezorm.rdb.render.SqlAppender;
import org.hsweb.ezorm.rdb.simple.wrapper.BeanWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Predicate;

import static org.hsweb.web.starter.SystemVersion.Property.*;


/**
 * @author zhouhao
 */
public class SystemInitialize implements InitializingBean {
    private static final Logger              logger       = LoggerFactory.getLogger(SystemInitialize.class);
    static final         Predicate<Resource> allWayIsTrue = (resource) -> true;

    private SqlExecutor   sqlExecutor;
    private RDBDatabase   database;
    private String        databaseUserName;
    private SystemVersion targetVersion;
    private SystemVersion installedVersion;
    private String installGroovyScriptPackage = "classpath*:org/hsweb/start/scripts/install/install.groovy";
    private String installSqlScriptPackage    = "classpath*:org/hsweb/start/scripts/install/sql/{db}/install.sql";

    private String upgradeGroovyScriptPackage = "classpath*:org/hsweb/start/scripts/upgrade/*.groovy";
    private String upgradeSqlScriptPackage    = "classpath*:org/hsweb/start/scripts/upgrade/sql/{db}/*.sql";

    private String initializeScriptPackage = "classpath*:org/hsweb/start/scripts/initialize/initialize.groovy";
    private String initializeSqlPackage    = "classpath*:org/hsweb/start/scripts/initialize/sql/{db}/initialize.sql";

    private String customInitializeSqlScriptPackage = "classpath*:scripts/install/sql/{db}/*.sql";
    private String customInitializeScriptPackage    = "classpath*:scripts/initialize/initialize.groovy";

    private String customUpgradeSqlScriptPackage = "classpath*:scripts/upgrade/sql/{db}/*.sql";
    private String customUpgradeScriptPackage    = "classpath*:scripts/upgrade/*.groovy";

    public SystemInitialize(SystemVersion targetVersion,
                            SqlExecutor sqlExecutor,
                            RDBDatabase database,
                            String databaseUserName, String dbType) {
        Assert.notNull(sqlExecutor);
        Assert.notNull(database);
        Assert.notNull(databaseUserName);
        Assert.notNull(targetVersion);
        this.sqlExecutor = sqlExecutor;
        this.database = database;
        this.databaseUserName = databaseUserName;
        this.targetVersion = targetVersion;
        installSqlScriptPackage = installSqlScriptPackage.replace("{db}", dbType);
        upgradeSqlScriptPackage = upgradeSqlScriptPackage.replace("{db}", dbType);
        initializeSqlPackage = initializeSqlPackage.replace("{db}", dbType);
        customInitializeSqlScriptPackage = customInitializeSqlScriptPackage.replace("{db}", dbType);
        customUpgradeSqlScriptPackage = customUpgradeSqlScriptPackage.replace("{db}", dbType);
    }


    Predicate<Resource> systemVersionFilter = (resource) -> {
        try {
            String name = resource.getFilename();
            name = name.substring(0, name.lastIndexOf("."));
            boolean snapshot = name.toLowerCase().contains("snapshot");
            name = name.toLowerCase().replace(".snapshot", "").replace("-snapshot", "");

            String[] ver = name.split("[.]");


            SystemVersion systemVersion = new SystemVersion();
            systemVersion.setVersion(Integer.parseInt(ver[0])
                    , Integer.parseInt(ver[1])
                    , Integer.parseInt(ver[2])
                    , snapshot);
            boolean install = systemVersion.compareTo(targetVersion) <= 0;
            if (installedVersion != null) {
                install = systemVersion.compareTo(installedVersion) == 1;
            }
            return install;
        } catch (Exception e) {
            logger.warn("parse file {} version error", resource, e);
            return false;
        }
    };

    Predicate<Resource> frameworkVersionFilter = (resource) -> {
        try {
            String name = resource.getFilename();
            name = name.substring(0, name.lastIndexOf("."));
            boolean snapshot = name.toLowerCase().contains("snapshot");
            name = name.toLowerCase().replace(".snapshot", "").replace("-snapshot", "");
            String[] ver = name.split("[.]");
            SystemVersion.FrameworkVersion systemVersion = new SystemVersion.FrameworkVersion();
            systemVersion.setVersion(Integer.parseInt(ver[0]),
                    Integer.parseInt(ver[1]),
                    Integer.parseInt(ver[2]), snapshot);
            boolean install = systemVersion.compareTo(targetVersion.getFrameworkVersion()) <= 0;
            if (installedVersion != null) {
                install = systemVersion.compareTo(installedVersion.getFrameworkVersion()) == 1;
            }
            return install;
        } catch (Exception e) {
            logger.warn("parse file {} version error", resource, e);
            return false;
        }
    };


    protected void install() throws Exception {
        boolean sync = false;
        if (installedVersion == null) {
            tryInitDatabase();
            tryUpgradeFramework();
            tryCustomInit();
            tryCustomUpgrade();
            sync = true;
        } else {
            int frameworkCompare = installedVersion.getFrameworkVersion().compareTo(targetVersion.getFrameworkVersion());
            int systemCompare = installedVersion.compareTo(targetVersion);

            if (frameworkCompare > 0) {
                logger.warn("The installation framework ({}) is newer than the new version ({}).", installedVersion.getFrameworkVersion(), targetVersion.getFrameworkVersion());
            } else if (frameworkCompare < 0) {
                tryUpgradeFramework();
                sync = true;
            } else {
                if (logger.isInfoEnabled())
                    logger.info("framework : {}", installedVersion.getFrameworkVersion());
            }

            if (systemCompare > 0) {
                logger.warn("The installation ({}) is newer than the new version ({}).", installedVersion, targetVersion);
            } else if (systemCompare < 0) {
                tryCustomUpgrade();
                sync = true;
            } else {
                if (logger.isInfoEnabled())
                    logger.info("system : {}", installedVersion);
            }
        }
        if (sync)
            syncSystemVersion();
    }

    protected void tryInitDatabase() throws Exception {
        executeGroovy(getScriptFileContext(installGroovyScriptPackage, allWayIsTrue));
        executeSql(getScriptFileContext(installSqlScriptPackage, allWayIsTrue));
        executeGroovy(getScriptFileContext(initializeScriptPackage, allWayIsTrue));
        executeSql(getScriptFileContext(initializeSqlPackage, allWayIsTrue));
    }

    protected void executeGroovy(List<String> groovyScript) throws Exception {
        DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine("groovy");
        String id = "hsweb.install";
        Map<String, Object> var = getScriptVar();
        for (String s : groovyScript) {
            if (StringUtils.isNullOrEmpty(s)) continue;
            engine.compile(id, s);
            ExecuteResult result = engine.execute(id, var);
            engine.remove(id);
            if (result.isSuccess()) {
                //logger.debug("install success!");
            } else {
                if (result.getException() != null) {
                    throw (Exception) result.getException();
                } else {
                    throw new RuntimeException("execute groovy script error!");
                }
            }
        }
    }

    protected Map<String, Object> getScriptVar() {
        Map<String, Object> var = new HashMap<>();
        var.put("database", database);
        var.put("sqlExecutor", sqlExecutor);
        return var;
    }

    protected void tryUpgradeFramework() throws Exception {
        executeGroovy(getScriptFileContext(upgradeGroovyScriptPackage, frameworkVersionFilter));
        executeSql(getScriptFileContext(upgradeSqlScriptPackage, frameworkVersionFilter));
    }


    protected void tryCustomInit() throws Exception {
        executeGroovy(getScriptFileContext(customInitializeScriptPackage, allWayIsTrue));
        executeSql(getScriptFileContext(customInitializeSqlScriptPackage, allWayIsTrue));
    }

    protected void tryCustomUpgrade() throws Exception {
        executeGroovy(getScriptFileContext(customUpgradeScriptPackage, systemVersionFilter));
        executeSql(getScriptFileContext(customUpgradeSqlScriptPackage, systemVersionFilter));
    }

    protected void executeSql(List<String> sqlFile) throws SQLException {
        for (String sql : sqlFile) {
            if (StringUtils.isNullOrEmpty(sql)) continue;
            sqlExecutor.exec(sql);
        }
    }

    protected void syncSystemVersion() throws SQLException {
        RDBTable<SystemVersion> rdbTable = database.getTable("s_system");
        int total = rdbTable.createQuery().total();
        if (total == 0) {
            rdbTable.createInsert().value(targetVersion).exec();
        } else {
            rdbTable.createUpdate().set(targetVersion).where().sql("1=1").exec();
        }
    }

    protected SystemVersion getSystemVersion() throws SQLException {
        boolean tableInstall = sqlExecutor.tableExists("s_system");
        database.createOrAlter("s_system")
                .addColumn().name("name").varchar(128).notNull().comment("系统名称").commit()
                .addColumn().name("major_version").alias(majorVersion).number(32).javaType(Integer.class).notNull().comment("主版本号").commit()
                .addColumn().name("minor_version").alias(minorVersion).number(32).javaType(Integer.class).notNull().comment("次版本号").commit()
                .addColumn().name("revision_version").alias(revisionVersion).number(32).javaType(Integer.class).notNull().comment("修订版").commit()
                .addColumn().name("snapshot").number(1).javaType(Boolean.class).notNull().comment("是否快照版").commit()
                .addColumn().name("comment").varchar(2000).comment("系统说明").commit()
                .addColumn().name("website").varchar(2000).comment("系统网址").commit()
                .addColumn().name("framework_version").notNull().alias(frameworkVersion).clob()
                .custom(column -> column.setValueConverter(new JSONValueConverter(SystemVersion.FrameworkVersion.class, new ClobValueConverter()))).notNull().comment("框架版本").commit()
                .comment("系统信息")
                .custom(table -> table.setObjectWrapper(new BeanWrapper<SystemVersion>(SystemVersion::new, table)))
                .commit();

        if (!tableInstall) {
            if (!sqlExecutor.tableExists("s_user")) {
                return null;
            } else {
                logger.warn("Database Already initialized,but table [s_system] not Exists!");
                //直接同步数据库
                syncSystemVersion();
                return targetVersion;
            }
        }
        RDBTable<SystemVersion> rdbTable = database.getTable("s_system");
        return rdbTable.createQuery().single();
    }

    private int compareFileName(Resource resource, Resource target) {
        String name = resource.getFilename().split("[.]", 1)[0];
        String targetName = target.getFilename().split("[.]", 1)[0];

        if (StringUtils.isDouble(name) && StringUtils.isDouble(targetName)) {
            return ((Double) Double.parseDouble(name)).compareTo(Double.parseDouble(targetName));
        }
        return name.compareTo(targetName);
    }

    protected List<String> getScriptFileContext(String filePackage, Predicate<Resource> filter) throws IOException {
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(filePackage);
        List<String> scripts = new ArrayList<>();
        Arrays.stream(resources)
                .filter(filter)
                .sorted(this::compareFileName)
                .forEach(resource -> {
                    String name = resource.getFilename();
                    try {
                        if (name.endsWith(".sql")) {
                            scripts.addAll(stream2sqlString(resource.getInputStream()));
                        } else
                            scripts.add(stream2string(resource.getInputStream()));
                    } catch (IOException e) {
                        logger.error("read file ({}) error", name, e);
                    }
                });
        return scripts;
    }


    protected List<String> stream2sqlString(InputStream stream) throws UnsupportedEncodingException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, "utf-8"));
        List<String> sqlList = new ArrayList<>();
        SqlAppender tmp = new SqlAppender();
        String uname = databaseUserName;
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
        return sqlList;
    }

    protected String stream2string(InputStream stream) throws IOException {
        return StreamUtils.copyToString(stream, Charset.forName("utf-8"));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.installedVersion = getSystemVersion();
        install();
    }
}
