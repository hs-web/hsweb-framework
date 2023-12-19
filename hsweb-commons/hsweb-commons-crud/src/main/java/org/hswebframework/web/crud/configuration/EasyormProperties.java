package org.hswebframework.web.crud.configuration;

import lombok.*;
import org.hswebframework.ezorm.rdb.metadata.RDBDatabaseMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBSchemaMetadata;
import org.hswebframework.ezorm.rdb.metadata.dialect.Dialect;
import org.hswebframework.ezorm.rdb.supports.h2.H2SchemaMetadata;
import org.hswebframework.ezorm.rdb.supports.mssql.SqlServerSchemaMetadata;
import org.hswebframework.ezorm.rdb.supports.mysql.MysqlSchemaMetadata;
import org.hswebframework.ezorm.rdb.supports.oracle.OracleSchemaMetadata;
import org.hswebframework.ezorm.rdb.supports.postgres.PostgresqlSchemaMetadata;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

@ConfigurationProperties(prefix = "easyorm")
@Data
public class EasyormProperties {

    private String defaultSchema = "PUBLIC";

    private String[] schemas = {};

    private boolean autoDdl = true;

    private boolean allowAlter = false;

    private boolean allowTypeAlter = true;

    /**
     * @see DialectProvider
     */
    private DialectProvider dialect = DialectEnum.h2;

    @Deprecated
    private Class<? extends Dialect> dialectType;

    @Deprecated
    private Class<? extends RDBSchemaMetadata> schemaType;

    @SneakyThrows
    public void setDialect(String dialect) {
        this.dialect = DialectProviders.lookup(dialect);
    }

    public RDBDatabaseMetadata createDatabaseMetadata() {
        RDBDatabaseMetadata metadata = new RDBDatabaseMetadata(createDialect());

        Set<String> schemaSet = new HashSet<>(Arrays.asList(schemas));
        if (defaultSchema != null) {
            schemaSet.add(defaultSchema);
        }
        schemaSet.stream()
                 .map(this::createSchema)
                 .forEach(metadata::addSchema);

        metadata.getSchema(defaultSchema)
                .ifPresent(metadata::setCurrentSchema);

        return metadata;
    }

    @SneakyThrows
    public RDBSchemaMetadata createSchema(String name) {
        return dialect.createSchema(name);
    }

    @SneakyThrows
    public Dialect createDialect() {
        return dialect.getDialect();
    }

    @Getter
    @AllArgsConstructor
    public enum DialectEnum implements DialectProvider {
        mysql(Dialect.MYSQL, "?") {
            @Override
            public RDBSchemaMetadata createSchema(String name) {
                return new MysqlSchemaMetadata(name);
            }
        },
        mssql(Dialect.MSSQL, "@arg") {
            @Override
            public RDBSchemaMetadata createSchema(String name) {
                return new SqlServerSchemaMetadata(name);
            }
        },
        oracle(Dialect.ORACLE, "?") {
            @Override
            public RDBSchemaMetadata createSchema(String name) {
                return new OracleSchemaMetadata(name);
            }

            @Override
            public String getValidationSql() {
                return "select 1 from dual";
            }
        },
        postgres(Dialect.POSTGRES, "$") {
            @Override
            public RDBSchemaMetadata createSchema(String name) {
                return new PostgresqlSchemaMetadata(name);
            }
        },
        h2(Dialect.H2, "$") {
            @Override
            public RDBSchemaMetadata createSchema(String name) {
                return new H2SchemaMetadata(name);
            }
        },
        ;

        private final Dialect dialect;
        private final String bindSymbol;

        public abstract RDBSchemaMetadata createSchema(String name);
    }
}
