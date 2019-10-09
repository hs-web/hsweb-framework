package org.hswebframework.web.crud.configuration;

import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;

public interface EntityTableMetadataResolver {

    RDBTableMetadata resolve(Class<?> entityClass);

}
