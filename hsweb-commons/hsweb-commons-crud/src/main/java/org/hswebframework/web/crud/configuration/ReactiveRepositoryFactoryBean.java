package org.hswebframework.web.crud.configuration;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.ezorm.rdb.mapping.defaults.DefaultReactiveRepository;
import org.hswebframework.ezorm.rdb.metadata.RDBSchemaMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
@Setter
public class ReactiveRepositoryFactoryBean<E, PK>
    implements FactoryBean<ReactiveRepository<E, PK>> {

    @Autowired
    private DatabaseOperator operator;

    @Autowired
    private EntityTableMetadataResolver resolver;

    private Class<E> entityType;

    @Autowired
    private EntityResultWrapperFactory wrapperFactory;

    @Override
    public ReactiveRepository<E, PK> getObject() {
        RDBTableMetadata table = resolver.resolve(entityType);
        String tableName = table.getName();
        return new DefaultReactiveRepository<>(
            operator,
            tableName,
            entityType,
            wrapperFactory.getWrapper(entityType));
    }

    @Override
    public Class<?> getObjectType() {
        return ReactiveRepository.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
