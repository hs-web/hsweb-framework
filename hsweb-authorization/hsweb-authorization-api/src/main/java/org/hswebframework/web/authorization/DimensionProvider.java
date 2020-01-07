package org.hswebframework.web.authorization;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DimensionProvider {

    Flux<? extends DimensionType> getAllType();

    Flux<? extends Dimension> getDimensionByUserId(String userId);

    Mono<? extends Dimension> getDimensionById(DimensionType type, String id);

    Flux<String> getUserIdByDimensionId(String dimensionId);

}
