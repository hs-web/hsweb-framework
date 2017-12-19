package org.hswebframework.web.database.manager.meta.table.parser;

import org.hswebframework.web.database.manager.meta.ObjectMetadata;
import org.hswebframework.web.datasource.DatabaseType;

public interface MetaDataParserSupplier<M extends ObjectMetadata>  {
    boolean isSupport(DatabaseType type);

    MetaDataParser<M> get();
}
