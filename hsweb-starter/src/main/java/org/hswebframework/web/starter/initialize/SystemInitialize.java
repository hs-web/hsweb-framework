package org.hswebframework.web.starter.initialize;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.expands.script.engine.DynamicScriptEngine;
import org.hswebframework.expands.script.engine.DynamicScriptEngineFactory;
import org.hswebframework.ezorm.rdb.codec.ClobValueCodec;
import org.hswebframework.ezorm.rdb.codec.CompositeValueCodec;
import org.hswebframework.ezorm.rdb.codec.JsonValueCodec;
import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.hswebframework.ezorm.rdb.mapping.defaults.record.Record;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.web.bean.FastBeanCopier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StreamUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 */
public class SystemInitialize {
    private final Logger logger = LoggerFactory.getLogger(SystemInitialize.class);

    private final DatabaseOperator database;
    //将要安装的信息
    private final SystemVersion targetVersion;

    //已安装的信息
    private SystemVersion installed;

    private List<SimpleDependencyInstaller> readyToInstall = new ArrayList<>();

    @Setter
    @Getter
    private List<String> excludeTables;

    private String installScriptPath = "classpath*:hsweb-starter.js";

    private Map<String, Object> scriptContext = new HashMap<>();

    private boolean initialized = false;

    private SyncRepository<Record, String> system;

    public SystemInitialize(DatabaseOperator database, SystemVersion targetVersion) {
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

    protected void syncSystemVersion() {
        Map<String, Object> mapVersion = FastBeanCopier.copy(targetVersion, HashMap::new);

        if (installed == null) {
            system.insert(Record.newRecord(mapVersion));
        } else {

            //合并已安装的依赖
            //修复如果删掉了依赖，再重启会丢失依赖信息的问题
            for (Dependency dependency : installed.getDependencies()) {
                Dependency target = targetVersion.getDependency(dependency.getGroupId(), dependency.getArtifactId());
                if (target == null) {
                    targetVersion.getDependencies().add(dependency);
                }
            }
            mapVersion = FastBeanCopier.copy(targetVersion, HashMap::new);
            system.createUpdate().set(Record.newRecord(mapVersion))
                    .where(dsl -> dsl.is(targetVersion::getName))
                    .execute();
        }
    }

    protected Map<String, Object> getScriptContext() {
        return new HashMap<>(scriptContext);
    }

    protected void doInstall() {
        List<SimpleDependencyInstaller> doInitializeDep = new ArrayList<>();
        List<Dependency> installedDependencies =
                readyToInstall.stream().map(installer -> {
                    Dependency dependency = installer.getDependency();
                    Dependency installed = getInstalledDependency(dependency.getGroupId(), dependency.getArtifactId());
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

    private Dependency getInstalledDependency(String groupId, String artifactId) {
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
                .addColumn().name("major_version").alias("majorVersion").integer().comment("主版本号").commit()
                .addColumn().name("minor_version").alias("minorVersion").integer().comment("次版本号").commit()
                .addColumn().name("revision_version").alias("revisionVersion").integer().comment("修订版").commit()
                .addColumn().name("comment").varchar(2000).comment("系统说明").commit()
                .addColumn().name("website").varchar(2000).comment("系统网址").commit()
                .addColumn().name("framework_version").notNull().alias("frameworkVersion").clob()
                .custom(column ->
                        column.setValueCodec(new CompositeValueCodec()
                                .addEncoder(JsonValueCodec.of(SystemVersion.FrameworkVersion.class))
                                .addDecoder(ClobValueCodec.INSTANCE)
                                .addDecoder(JsonValueCodec.of(SystemVersion.FrameworkVersion.class)))).notNull().comment("框架版本").commit()
                .addColumn().name("dependencies").notNull().alias("dependencies").clob()
                .custom(column -> column.setValueCodec(new CompositeValueCodec()
                        .addEncoder(JsonValueCodec.ofCollection(List.class, Dependency.class))
                        .addDecoder(ClobValueCodec.INSTANCE)
                        .addDecoder(JsonValueCodec.ofCollection(List.class, Dependency.class)))).notNull().comment("依赖详情").commit()
                .comment("系统信息")
                .commit()
                .sync();
        system = database.dml().createRepository("s_system");

        if (!tableInstall) {
            installed = null;
            return;
        }

        installed = system.createQuery()
                .where(dsl -> dsl.is("name", targetVersion.getName()))
                .paging(0, 1)
                .fetchOne()
                .map(r -> FastBeanCopier.copy(r, SystemVersion::new))
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
