package org.hswebframework.web.crud.web;

import io.swagger.v3.oas.annotations.Operation;
import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.DeleteAction;
import org.hswebframework.web.exception.NotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collections;

public interface DeleteController<E, K> {
    @Authorize(ignore = true)
    SyncRepository<E, K> getRepository();

    @DeleteMapping("/{id:.+}")
    @DeleteAction
    @Operation(summary = "根据ID删除")
    default E delete(@PathVariable K id) {
        E data = getRepository()
                .findById(id)
                .orElseThrow(NotFoundException::new);
        getRepository().deleteById(Collections.singletonList(id));
        return data;
    }
}
