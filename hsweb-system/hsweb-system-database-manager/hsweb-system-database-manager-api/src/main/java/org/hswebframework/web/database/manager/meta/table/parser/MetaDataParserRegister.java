package org.hswebframework.web.database.manager.meta.table.parser;

import org.hswebframework.web.database.manager.meta.ObjectMetadata;
import org.hswebframework.web.datasource.DatabaseType;

public interface MetaDataParserRegister {
    <M extends ObjectMetadata> void registerMetaDataParser(DatabaseType databaseType, ObjectMetadata.ObjectType objectType, MetaDataParser<M> parser);
}
