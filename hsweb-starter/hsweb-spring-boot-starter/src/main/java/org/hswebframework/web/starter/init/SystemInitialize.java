package org.hswebframework.web.starter.init;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.RDBDatabase;
import org.hswebframework.ezorm.rdb.RDBTable;
import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.ezorm.rdb.meta.converter.ClobValueConverter;
import org.hswebframework.ezorm.rdb.meta.converter.JSONValueConverter;
import org.hswebframework.ezorm.rdb.simple.wrapper.BeanWrapper;
import org.hswebframework.expands.script.engine.DynamicScriptEngine;
import org.hswebframework.expands.script.engine.DynamicScriptEngineFactory;
import org.hswebframework.web.starter.SystemVersion;
import org.hswebframework.web.starter.init.simple.SimpleDependencyInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.hswebframework.web.starter.SystemVersion.Property.*;

/**
 * @author zhouhao
 */
public class SystemInitialize {
    private Logger logger = LoggerFactory.getLogger(SystemInitialize.class);

    private SqlExecutor   sqlExecutor;
    private RDBDatabase   database;
    //将要安装的信息
    private SystemVersion targetVersion;

    //已安装的信息
    private SystemVersion installed;

    private List<SimpleDependencyInstaller> readyToInstall;

    @Setter
    @Getter
    private List<String> excludeTables;

    private String installScriptPath = "classpath*:hsweb-starter.js";

    private Map<String, Object> scriptContext = new HashMap<>();

    private boolean initialized = false;


    public SystemInitialize(SqlExecutor sqlExecutor, RDBDatabase database, SystemVersion targetVersion) {
        this.sqlExecutor = sqlExecutor;
        this.database = database;
        this.targetVersion = targetVersion;
    }


    public void init() {
        if (initialized) {
            return;
        }
        if (!CollectionUtils.isEmpty(excludeTables)) {
            this.database = new SkipCreateOrAlterRDBDatabase(database, excludeTables, sqlExecutor);
        }
        scriptContext.put("sqlExecutor", sqlExecutor);
        scriptContext.put("database", database);
        scriptContext.put("logger", logger);
        initialized = true;
    }

    public void addScriptContext(String var, Object val) {
        scriptContext.put(var, val);
    }

    protected void syncSystemVersion() throws SQLException {
        RDBTable<SystemVersion> rdbTable = database.getTable("s_system");
        if (installed == null) {
            rdbTable.createInsert().value(targetVersion).exec();
        } else {
            //合并已安装的依赖
            //修复如果删掉了依赖，再重启会丢失依赖信息的问题
            for (SystemVersion.Dependency dependency : installed.getDependencies()) {
                SystemVersion.Dependency target = targetVersion.getDependency(dependency.getGroupId(), dependency.getArtifactId());
                if (target == null) {
                    targetVersion.getDependencies().add(dependency);
                }
            }

            rdbTable.createUpdate().set(targetVersion).where().is("name", targetVersion.getName()).exec();
        }
    }

    protected Map<String, Object> getScriptContext() {
        return new HashMap<>(scriptContext);
    }

    protected void doInstall() {
        List<SimpleDependencyInstaller> doInitializeDep = new ArrayList<>();
        List<SystemVersion.Dependency> installedDependencies =
                readyToInstall.stream().map(installer -> {
                    SystemVersion.Dependency dependency = installer.getDependency();
                    SystemVersion.Dependency installed = getInstalledDependency(dependency.getGroupId(), dependency.getArtifactId());
                    //安装依赖
                    if (installed == null) {
                        doInitializeDep.add(installer);
                        installer.doInstall(getScriptContext());
                    }
                    //更新依赖
                    if (installed == null || installed.compareTo(dependency) < 0) {
                        installer.doUpgrade(getScriptContext(), installed);
                    }
                    return dependency;
                }).collect(Collectors.toList());

        for (SimpleDependencyInstaller installer : doInitializeDep) {
            installer.doInitialize(getScriptContext());
        }
        targetVersion.setDependencies(installedDependencies);
    }

    private SystemVersion.Dependency getInstalledDependency(String groupId, String artifactId) {
        if (installed == null) {
            return null;
        }
        return installed.getDependency(groupId, artifactId);
    }

    private SimpleDependencyInstaller getReadyToInstallDependency(String groupId, String artifactId) {
        if (readyToInstall == null) {
            return null;
        }
        return readyToInstall.stream()
                .filter(installer -> installer.getDependency().isSameDependency(groupId, artifactId))
                .findFirst().orElse(null);
    }

    private void initReadyToInstallDependencies() {
        DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine("js");
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(installScriptPath);
            List<SimpleDependencyInstaller> installers = new ArrayList<>();
            for (Resource resource : resources) {
                String script = StreamUtils.copyToString(resource.getInputStream(), Charset.forName("utf-8"));
                SimpleDependencyInstaller installer = new SimpleDependencyInstaller();
                engine.compile("__tmp", script);
                Map<String, Object> context = getScriptContext();
                context.put("dependency", installer);
                engine.execute("__tmp", context).getIfSuccess();
                installers.add(installer);
            }
            readyToInstall = installers;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            engine.remove("__tmp");
        }

    }

    protected void initInstallInfo() throws SQLException {
        boolean tableInstall = sqlExecutor.tableExists("s_system");
        database.createOrAlter("s_system")
                .addColumn().name("name").varchar(128).comment("系统名称").commit()
                .addColumn().name("major_version").alias(majorVersion).number(32).javaType(Integer.class).comment("主版本号").commit()
                .addColumn().name("minor_version").alias(minorVersion).number(32).javaType(Integer.class).comment("次版本号").commit()
                .addColumn().name("revision_version").alias(revisionVersion).number(32).javaType(Integer.class).comment("修订版").commit()
                .addColumn().name("snapshot").number(1).javaType(Boolean.class).comment("是否快照版").commit()
                .addColumn().name("comment").varchar(2000).comment("系统说明").commit()
                .addColumn().name("website").varchar(2000).comment("系统网址").commit()
                .addColumn().name("framework_version").notNull().alias(frameworkVersion).clob()
                .custom(column -> column.setValueConverter(new JSONValueConverter(SystemVersion.FrameworkVersion.class, new ClobValueConverter()))).notNull().comment("框架版本").commit()
                .addColumn().name("dependencies").notNull().alias(dependencies).clob()
                .custom(column -> column.setValueConverter(new JSONValueConverter(SystemVersion.Dependency.class, new ClobValueConverter()))).notNull().comment("依赖详情").commit()
                .comment("系统信息")
                .custom(table -> table.setObjectWrapper(new BeanWrapper<SystemVersion>(SystemVersion::new, table)))
                .commit();

        if (!tableInstall) {
            installed = null;
            return;
        }
        RDBTable<SystemVersion> rdbTable = database.getTable("s_system");
        installed = rdbTable.createQuery().where("name", targetVersion.getName()).single();
    }


    public void install() throws Exception {
        init();
        initInstallInfo();
        initReadyToInstallDependencies();
        doInstall();
        syncSystemVersion();
    }
}
