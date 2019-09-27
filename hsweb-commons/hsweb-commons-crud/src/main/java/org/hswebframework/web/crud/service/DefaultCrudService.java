package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultCrudService<E,K> implements CrudService<E,K> {

    @Autowired
    private SyncRepository<E, K> repository;

    @Override
    public SyncRepository<E, K> getRepository() {
        return repository;
    }

}
