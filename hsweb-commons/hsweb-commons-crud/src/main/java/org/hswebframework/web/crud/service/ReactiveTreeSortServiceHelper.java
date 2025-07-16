package org.hswebframework.web.crud.service;

import org.hswebframework.web.api.crud.entity.TreeSortSupportEntity;
import org.hswebframework.web.id.IDGenerator;
import reactor.core.publisher.Flux;

import java.util.*;

public class ReactiveTreeSortServiceHelper<E extends TreeSortSupportEntity<PK>, PK> extends TreeSortServiceHelper<E, PK> {

    private final ReactiveTreeSortEntityService<E, PK> service;

    public  ReactiveTreeSortServiceHelper(ReactiveTreeSortEntityService<E, PK> service) {
        this.service = service;
    }

    @Override
    protected IDGenerator<PK> getIdGenerator() {
        return service.getIDGenerator();
    }

    @Override
    protected void applyChildren(E parent, List<E> children) {
        service.setChildren(parent, children);
    }

    @Override
    protected boolean isRootNode(E node) {
        return service.isRootNode(node);
    }

    @Override
    protected Flux<E> queryIncludeChildren(Collection<PK> idList) {
        return service.queryIncludeChildren(idList);
    }

    @Override
    protected Flux<E> queryById(Collection<PK> idList) {
        return service.findById(idList);
    }
}
