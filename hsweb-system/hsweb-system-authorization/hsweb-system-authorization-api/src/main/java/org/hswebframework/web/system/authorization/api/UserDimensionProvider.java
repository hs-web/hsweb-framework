package org.hswebframework.web.system.authorization.api;

import org.hswebframework.web.authorization.DefaultDimensionType;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.DimensionProvider;
import org.hswebframework.web.authorization.DimensionType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class UserDimensionProvider implements DimensionProvider {

    @Override
    public Flux<DimensionType> getAllType() {
        return Flux.just(DefaultDimensionType.user);
    }

    @Override
    public Flux<Dimension> getDimensionByUserId(String userId) {
        return Flux.just(userId)
                .map(id -> Dimension.of(userId, userId, DefaultDimensionType.user));
    }

    @Override
    public Mono<? extends Dimension> getDimensionById(DimensionType type, String id) {
        return Mono.just(id)
                .map(userId -> Dimension.of(userId, userId, DefaultDimensionType.user));
    }

    @Override
    public Flux<String> getUserIdByDimensionId(String dimensionId) {
        return Flux.just(dimensionId);
    }
}
