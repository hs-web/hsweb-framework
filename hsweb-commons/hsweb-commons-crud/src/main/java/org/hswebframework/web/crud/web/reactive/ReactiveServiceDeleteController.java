package org.hswebframework.web.crud.web.reactive;

import io.swagger.v3.oas.annotations.Operation;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.DeleteAction;
import org.hswebframework.web.crud.service.ReactiveCrudService;
import org.hswebframework.web.exception.NotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

public interface ReactiveServiceDeleteController<E, K> {
    @Authorize(ignore = true)
    ReactiveCrudService<E, K> getService();

    @DeleteMapping("/{id:.+}")
    @DeleteAction
    @Operation(summary = "根据ID删除")
    default Mono<E> delete(@PathVariable K id) {
        return getService()
                .findById(Mono.just(id))
                .switchIfEmpty(Mono.error(NotFoundException::new))
                .flatMap(e -> getService()
                        .deleteById(Mono.just(id))
                        .thenReturn(e));
    }
}
