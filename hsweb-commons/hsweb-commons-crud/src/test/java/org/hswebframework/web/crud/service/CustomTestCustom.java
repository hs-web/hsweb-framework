package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.metadata.DataType;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.web.crud.configuration.TableMetadataCustomizer;
import org.hswebframework.web.crud.entity.CustomTestEntity;
import org.hswebframework.web.crud.entity.TestEntity;
import org.hswebframework.web.crud.entity.factory.EntityMappingCustomizer;
import org.hswebframework.web.crud.entity.factory.MapperEntityFactory;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.JDBCType;
import java.util.Set;

@Component
public class CustomTestCustom implements EntityMappingCustomizer, TableMetadataCustomizer {
    @Override
    public void custom(MapperEntityFactory factory) {
        factory.addMapping(TestEntity.class, new MapperEntityFactory.Mapper<>(CustomTestEntity.class, CustomTestEntity::new));
    }

    @Override
    public void customColumn(Class<?> entityType,
                             PropertyDescriptor descriptor,
                             Field field,
                             Set<Annotation> annotations,
                             RDBColumnMetadata column) {

    }

    @Override
    public void customTable(Class<?> entityType, RDBTableMetadata table) {
        if (TestEntity.class.isAssignableFrom(entityType)) {

            RDBColumnMetadata col = table.newColumn();
            col.setName("ext_name");
            col.setAlias("extName");
            col.setLength(32);
            col.setType(DataType.jdbc(JDBCType.VARCHAR, String.class));
            table.addColumn(col);

        }
    }
}
