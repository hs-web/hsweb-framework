package org.hswebframework.web.crud.service;

import org.hswebframework.web.api.crud.entity.TreeSortSupportEntity;
import org.hswebframework.web.id.IDGenerator;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SyncTreeSortServiceHelper<E extends TreeSortSupportEntity<PK>, PK> extends TreeSortServiceHelper<E, PK> {

    private final TreeSortEntityService<E, PK> service;

    public SyncTreeSortServiceHelper(TreeSortEntityService<E, PK> service) {
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

    public List<E> prepare(Collection<E> source) {
        return super
                .prepare(Flux.fromIterable(source))
                .toStream()
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("all")
    protected Flux<E> queryIncludeChildren(Collection<PK> idList) {
        return Flux.fromIterable(service.queryIncludeChildren(idList));
    }

    @Override
    @SuppressWarnings("all")
    protected Flux<E> queryById(Collection<PK> idList) {
        return Flux.fromIterable(service.findById(idList));
    }
}
