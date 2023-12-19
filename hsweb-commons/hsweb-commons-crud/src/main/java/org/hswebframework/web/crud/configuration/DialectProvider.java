package org.hswebframework.web.crud.configuration;

import org.hswebframework.ezorm.rdb.metadata.RDBSchemaMetadata;
import org.hswebframework.ezorm.rdb.metadata.dialect.Dialect;

public interface DialectProvider {

    String name();

    Dialect getDialect();

    String getBindSymbol();

    RDBSchemaMetadata createSchema(String name);

    default String getValidationSql(){
        return "select 1";
    }
}
