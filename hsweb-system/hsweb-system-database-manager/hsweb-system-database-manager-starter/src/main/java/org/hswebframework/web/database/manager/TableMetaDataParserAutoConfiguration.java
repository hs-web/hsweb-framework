package org.hswebframework.web.database.manager;

import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.database.manager.meta.ObjectMetadata;
import org.hswebframework.web.database.manager.meta.table.parser.MetaDataParserRegister;
import org.hswebframework.web.database.manager.meta.table.parser.MetaDataParserSupplier;
import org.hswebframework.web.database.manager.meta.table.parser.TableMetaDataParser;
import org.hswebframework.web.database.manager.meta.table.parser.support.H2TableMetaDataParser;
import org.hswebframework.web.database.manager.meta.table.parser.support.MysqlTableMetaDataParser;
import org.hswebframework.web.database.manager.meta.table.parser.support.OracleTableMetaDataParser;
import org.hswebframework.web.database.manager.meta.table.parser.support.SqlServerTableMetaDataParser;
import org.hswebframework.web.datasource.DatabaseType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TableMetaDataParserAutoConfiguration {

    @Autowired
    private SqlExecutor sqlExecutor;

    @Autowired
    private MetaDataParserRegister metaDataParserRegister;

    @Bean
    @ConditionalOnClass(name = "org.h2.Driver")
    public H2TableMetaDataParser h2TableMetaDataParser() {
        return new H2TableMetaDataParser(sqlExecutor);
    }

    @Bean
    @ConditionalOnClass(name = "com.mysql.jdbc.Driver")
    public MysqlTableMetaDataParser mysqlTableMetaDataParser() {
        return new MysqlTableMetaDataParser(sqlExecutor);
    }

    @Bean
    @ConditionalOnClass(name = "oracle.jdbc.driver.OracleDriver")
    public OracleTableMetaDataParser oracleTableMetaParser() {
        return new OracleTableMetaDataParser(sqlExecutor);
    }

    @Bean
    @ConditionalOnClass(name = "com.microsoft.sqlserver.jdbc.SQLServerDriver")
    public SqlServerTableMetaDataParser sqlServerTableMetaDataParser() {
        return new SqlServerTableMetaDataParser(sqlExecutor);
    }

    @Bean
    @ConditionalOnClass(name = "net.sourceforge.jtds.jdbc.Driver")
    public SqlServerTableMetaDataParser jstdSqlServerTableMetaDataParser() {
        return new SqlServerTableMetaDataParser(sqlExecutor);
    }

    @Bean
    public BeanPostProcessor tableMetaDataAutoParserRegister() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
                return o;
            }

            @Override
            @SuppressWarnings("unchecked")
            public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
                if (o instanceof MetaDataParserSupplier) {
                    MetaDataParserSupplier<? extends TableMetaDataParser> supplier = ((MetaDataParserSupplier) o);
                    for (DatabaseType databaseType : DatabaseType.values()) {
                        if (supplier.isSupport(databaseType)) {
                            metaDataParserRegister.registerMetaDataParser(databaseType, ObjectMetadata.ObjectType.TABLE, supplier.get());
                        }
                    }
                }
                return o;
            }
        };
    }
}