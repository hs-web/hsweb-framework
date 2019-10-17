package org.hswebframework.web.system.authorization.api;

import org.hswebframework.web.authorization.DefaultDimensionType;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.DimensionProvider;
import reactor.core.publisher.Flux;

public class UserDimensionProvider implements DimensionProvider {

    @Override
    public Flux<Dimension> getDimensionByUserId(String userId) {
        return Flux.just(userId)
                .map(id -> Dimension.of(userId, userId, DefaultDimensionType.user));
    }

    @Override
    public Flux<String> getUserIdByDimensionId(String dimensionId) {
        return Flux.just(dimensionId);
    }
}
