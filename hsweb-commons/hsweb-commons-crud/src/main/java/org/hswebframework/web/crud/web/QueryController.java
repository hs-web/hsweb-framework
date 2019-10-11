package org.hswebframework.web.crud.web;

import org.hswebframework.web.crud.service.CrudService;

public interface QueryController<E,K> {

    CrudService<E,K> getService();

}
