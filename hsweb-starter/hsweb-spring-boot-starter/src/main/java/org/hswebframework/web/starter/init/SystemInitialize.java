package org.hswebframework.web.starter.init;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.expands.script.engine.DynamicScriptEngine;
import org.hswebframework.expands.script.engine.DynamicScriptEngineFactory;
import org.hswebframework.ezorm.rdb.codec.JsonValueCodec;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers;
import org.hswebframework.ezorm.rdb.mapping.wrapper.EntityResultWrapper;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.starter.SystemVersion;
import org.hswebframework.web.starter.init.simple.SimpleDependencyInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StreamUtils;

import java.nio.charset.Charset;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers.*;
import static org.hswebframework.web.starter.SystemVersion.Property.*;

/**
 * @author zhouhao
 */
public class SystemInitialize {
    private Logger logger = LoggerFactory.getLogger(SystemInitialize.class);

    private DatabaseOperator database;
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


    public SystemInitialize(SyncSqlExecutor sqlExecutor, DatabaseOperator database, SystemVersion targetVersion) {
        this.database = database;
        this.targetVersion = targetVersion;
    }


    public void init() {
        if (initialized) {
            return;
        }
//        if (!CollectionUtils.isEmpty(excludeTables)) {
//            this.database = new SkipCreateOrAlterRDBDatabase(database, excludeTables, sqlExecutor);
//        }
        scriptContext.put("database", database);
        scriptContext.put("logger", logger);
        initialized = true;
    }

    public void addScriptContext(String var, Object val) {
        scriptContext.put(var, val);
    }

    protected void syncSystemVersion() throws SQLException {
        Map<String ,Object> mapVersion = FastBeanCopier.copy(targetVersion, HashMap::new);

        if (installed == null) {
            database.dml()
                    .insert("s_system")
                    .value(mapVersion)
                    .execute()
                    .sync();
        } else {
            //合并已安装的依赖
            //修复如果删掉了依赖，再重启会丢失依赖信息的问题
            for (SystemVersion.Dependency dependency : installed.getDependencies()) {
                SystemVersion.Dependency target = targetVersion.getDependency(dependency.getGroupId(), dependency.getArtifactId());
                if (target == null) {
                    targetVersion.getDependencies().add(dependency);
                }
            }
            database.dml()
                    .update("s_system")
                    .set(mapVersion)
                    .where(dsl -> dsl.is(targetVersion::getName))
                    .execute()
                    .sync();
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

    protected void initInstallInfo() {
        boolean tableInstall = database.getMetadata().getTable("s_system").isPresent();
        database.ddl().createOrAlter("s_system")
                .addColumn().name("name").varchar(128).comment("系统名称").commit()
                .addColumn().name("major_version").alias(majorVersion).integer().comment("主版本号").commit()
                .addColumn().name("minor_version").alias(minorVersion).integer().comment("次版本号").commit()
                .addColumn().name("revision_version").alias(revisionVersion).integer().comment("修订版").commit()
                .addColumn().name("snapshot").type(JDBCType.TINYINT, Boolean.class)
                .comment("是否快照版").commit()
                .addColumn().name("comment").varchar(2000).comment("系统说明").commit()
                .addColumn().name("website").varchar(2000).comment("系统网址").commit()
                .addColumn().name("framework_version").notNull().alias(frameworkVersion).clob()
                .custom(column -> column.setValueCodec(JsonValueCodec.of(SystemVersion.FrameworkVersion.class))).notNull().comment("框架版本").commit()
                .addColumn().name("dependencies").notNull().alias(dependencies).clob()
                .custom(column -> column.setValueCodec(JsonValueCodec.of(SystemVersion.Dependency.class))).notNull().comment("依赖详情").commit()
                .comment("系统信息")
                .commit()
                .sync();

        if (!tableInstall) {
            installed = null;
            return;
        }
        installed = database.dml().query("s_system")
                .where(dsl -> dsl.is("name", targetVersion.getName()))
                .paging(0, 1)
                .fetch(optional(single(new EntityResultWrapper<>(SystemVersion::new))))
                .sync()
                .orElse(null)
        ;
    }


    public void install() throws Exception {
        init();
        initInstallInfo();
        initReadyToInstallDependencies();
        doInstall();
        syncSystemVersion();
    }
}
