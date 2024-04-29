package org.hswebframework.web.crud.web.reactive;

import io.swagger.v3.oas.annotations.Operation;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.DeleteAction;
import org.hswebframework.web.exception.NotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

public interface ReactiveDeleteController<E, K> {
    @Authorize(ignore = true)
    ReactiveRepository<E, K> getRepository();

    @DeleteMapping("/{id:.+}")
    @DeleteAction
    @Operation(summary = "根据ID删除")
    default Mono<E> delete(@PathVariable K id) {
        return getRepository()
                .findById(Mono.just(id))
                .switchIfEmpty(Mono.error(NotFoundException.NoStackTrace::new))
                .flatMap(e -> getRepository()
                        .deleteById(Mono.just(id))
                        .thenReturn(e));
    }
}
