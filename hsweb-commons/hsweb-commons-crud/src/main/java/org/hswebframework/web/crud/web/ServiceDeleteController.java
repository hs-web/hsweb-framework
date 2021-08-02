package org.hswebframework.web.crud.web;

import io.swagger.v3.oas.annotations.Operation;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.DeleteAction;
import org.hswebframework.web.crud.service.CrudService;
import org.hswebframework.web.exception.NotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ServiceDeleteController<E, K> {
    @Authorize(ignore = true)
    CrudService<E, K> getService();

    @DeleteMapping("/{id:.+}")
    @DeleteAction
    @Operation(summary = "根据ID删除")
    default E delete(@PathVariable K id) {
        E data = getService()
                .findById(id)
                .orElseThrow(NotFoundException::new);
        getService()
                .deleteById(id);
        return data;
    }
}
