package org.hswebframework.web.crud.web;

public interface ServiceCrudController<E, K> extends
        ServiceSaveController<E, K>,
        ServiceQueryController<E, K>,
        ServiceDeleteController<E, K> {
}
