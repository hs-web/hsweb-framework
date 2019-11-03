package org.hswebframework.web.crud.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import org.hswebframework.ezorm.rdb.metadata.RDBDatabaseMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBSchemaMetadata;
import org.hswebframework.ezorm.rdb.metadata.dialect.Dialect;
import org.hswebframework.ezorm.rdb.supports.h2.H2SchemaMetadata;
import org.hswebframework.ezorm.rdb.supports.mssql.SqlServerSchemaMetadata;
import org.hswebframework.ezorm.rdb.supports.mysql.MysqlSchemaMetadata;
import org.hswebframework.ezorm.rdb.supports.oracle.OracleSchemaMetadata;
import org.hswebframework.ezorm.rdb.supports.postgres.PostgresqlSchemaMetadata;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ConfigurationProperties(prefix = "easyorm")
@Data
public class EasyormProperties {

    private String defaultSchema="PUBLIC";

    private String[] schemas = {};

    private boolean autoDdl = true;

    private boolean allowAlter = false;

    private boolean allowTypeAlter = true;

    private DialectEnum dialect = DialectEnum.h2;

    private Class<? extends Dialect> dialectType;

    private Class<? extends RDBSchemaMetadata> schemaType;

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
        if (schemaType == null) {
            return dialect.createSchema(name);
        }
        return schemaType.getConstructor(String.class).newInstance(name);
    }

    @SneakyThrows
    public Dialect createDialect() {
        if (dialectType == null) {
            return dialect.getDialect();
        }

        return dialectType.newInstance();
    }

    @Getter
    @AllArgsConstructor
    public enum DialectEnum {
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

        private Dialect dialect;
        private String bindSymbol;

        public abstract RDBSchemaMetadata createSchema(String name);
    }
}
