package org.hswebframework.web.crud.web;

public interface CrudController<E, K> extends
        SaveController<E, K>,
        QueryController<E, K>,
        DeleteController<E, K> {
}
