package org.hswebframework.web.starter;

import org.hswebframework.expands.script.engine.DynamicScriptEngine;
import org.hswebframework.expands.script.engine.DynamicScriptEngineFactory;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.reactive.ReactiveSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.reactive.ReactiveSyncSqlExecutor;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.web.starter.initialize.AppProperties;
import org.hswebframework.web.starter.initialize.SystemInitialize;
import org.hswebframework.web.starter.initialize.SystemVersion;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class HswebAutoConfiguration {

    private List<DynamicScriptEngine> engines;

    @Autowired
    private ApplicationContext applicationContext;


    @PostConstruct
    public void init() {
        engines = Stream.of("js", "groovy")
                .map(DynamicScriptEngineFactory::getEngine)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        addGlobalVariable("logger", LoggerFactory.getLogger("org.hswebframework.script"));

        addGlobalVariable("spring", applicationContext);
    }

    private void addGlobalVariable(String var, Object val) {
        engines.forEach(engine -> {
                    try {
                        engine.addGlobalVariable(Collections.singletonMap(var, val));
                    } catch (NullPointerException ignore) {
                    }
                }
        );
    }

    @Bean
    public CommandLineRunner systemInit(DatabaseOperator database,
                                        AppProperties properties) {

        addGlobalVariable("database", database);
        addGlobalVariable("sqlExecutor", database.getMetadata().getFeature(SyncSqlExecutor.ID)
                .orElseGet(() -> database.getMetadata().getFeature(ReactiveSqlExecutor.ID)
                        .map(ReactiveSyncSqlExecutor::of).orElse(null)));
        SystemVersion version = properties.build();
        return args -> {

            SystemInitialize initialize = new SystemInitialize(database, version);
            initialize.setExcludeTables(properties.getInitTableExcludes());
            initialize.install();
        };
    }

}
