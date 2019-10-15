package org.hswebframework.web.crud.web.reactive;

public interface ReactiveCrudController<E, K> extends
        ReactiveSaveController<E, K>,
        ReactiveQueryController<E, K>,
        ReactiveDeleteController<E, K> {
}
