package org.hswebframework.web.crud.web.reactive;

public interface ReactiveServiceCrudController<E, K> extends
        ReactiveServiceSaveController<E, K>,
        ReactiveServiceQueryController<E, K>,
        ReactiveServiceDeleteController<E, K> {


}
