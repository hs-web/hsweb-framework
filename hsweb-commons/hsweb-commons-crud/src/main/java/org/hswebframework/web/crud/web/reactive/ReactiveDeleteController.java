package org.hswebframework.web.crud.web.reactive;

import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.exception.NotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

public interface ReactiveDeleteController<E, K> {
    ReactiveRepository<E, K> getRepository();

    @DeleteMapping("/{id:.+}")
    default Mono<E> delete(@PathVariable K id) {
        return getRepository()
                .findById(Mono.just(id))
                .switchIfEmpty(Mono.error(NotFoundException::new))
                .flatMap(e -> getRepository()
                        .deleteById(Mono.just(id))
                        .thenReturn(e));
    }
}
