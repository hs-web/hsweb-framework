package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.api.crud.entity.TreeSortSupportEntity;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class GenericReactiveTreeSupportCrudService<E extends TreeSortSupportEntity<K>, K> implements ReactiveTreeSortEntityService<E, K> {

    private static final int SAVE_BUFFER_SIZE = Integer.getInteger("tree.save.buffer.size", 200);

    @Autowired
    private ReactiveRepository<E, K> repository;

    @Override
    public ReactiveRepository<E, K> getRepository() {
        return repository;
    }

    @Override
    public int getBufferSize() {
        return SAVE_BUFFER_SIZE;
    }
}
