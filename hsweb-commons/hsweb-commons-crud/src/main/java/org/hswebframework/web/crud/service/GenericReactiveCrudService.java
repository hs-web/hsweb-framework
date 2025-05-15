package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class GenericReactiveCrudService<E, K> implements ReactiveCrudService<E, K> {

    @Autowired
    @SuppressWarnings("all")
    private ReactiveRepository<E, K> repository;

    @Override
    public ReactiveRepository<E, K> getRepository() {
        return repository;
    }

    public GenericReactiveCrudService() {
    }

    public GenericReactiveCrudService(ReactiveRepository<E, K> repository) {
        this.repository = repository;
    }
}
