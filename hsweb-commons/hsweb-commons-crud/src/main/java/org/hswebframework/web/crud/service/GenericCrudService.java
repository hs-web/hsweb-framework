package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.hswebframework.web.api.crud.entity.TransactionManagers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public abstract class GenericCrudService<E,K> implements CrudService<E,K> {

    @Autowired
    private SyncRepository<E, K> repository;

    @Override
    public SyncRepository<E, K> getRepository() {
        return repository;
    }

}
