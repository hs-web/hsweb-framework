package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.hswebframework.web.api.crud.entity.TreeSortSupportEntity;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class GenericTreeSupportCrudService<E extends TreeSortSupportEntity<K>,K> implements TreeSortEntityService<E,K> {

    @Autowired
    private SyncRepository<E, K> repository;

    @Override
    public SyncRepository<E, K> getRepository() {
        return repository;
    }

}
