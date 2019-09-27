package org.hswebframework.web.starter.easyorm;

import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;

public interface EntityTableMetadataResolver {

    RDBTableMetadata resolve(Class<?> entityClass);

}
