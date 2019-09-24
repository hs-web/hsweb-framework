package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultReactiveCrudService<E,K> implements ReactiveCrudService<E,K> {

    @Autowired
    private ReactiveRepository<E, K> repository;

    @Override
    public ReactiveRepository<E, K> getRepository() {
        return repository;
    }

}
