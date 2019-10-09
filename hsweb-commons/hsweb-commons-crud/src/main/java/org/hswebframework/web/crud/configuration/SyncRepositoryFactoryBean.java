package org.hswebframework.web.crud.configuration;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.hswebframework.ezorm.rdb.mapping.defaults.DefaultSyncRepository;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
@Setter
public class SyncRepositoryFactoryBean<E, PK>
        implements FactoryBean<SyncRepository<E, PK>> {


    @Autowired
    private DatabaseOperator operator;

    @Autowired
    private EntityTableMetadataResolver resolver;

    private Class<E> entityType;

    @Autowired
    private EntityResultWrapperFactory wrapperFactory;

    @Override
    public SyncRepository<E, PK> getObject() {

        return new DefaultSyncRepository<>(operator,
                resolver.resolve(entityType),
                entityType,
                wrapperFactory.getWrapper(entityType));
    }

    @Override
    public Class<?> getObjectType() {
        return SyncRepository.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
