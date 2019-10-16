package org.hswebframework.web.system.authorization.api;

import org.hswebframework.web.authorization.DefaultDimensionType;
import reactor.core.publisher.Flux;

public class UserPermissionDimensionProvider implements PermissionDimensionProvider {

    @Override
    public Flux<PermissionDimension> getDimensionByUserId(String userId) {
        return Flux.just(userId)
                .map(id -> PermissionDimension.of(userId, DefaultDimensionType.user));
    }

    @Override
    public Flux<String> getUserIdByDimensionId(String dimensionId) {
        return Flux.just(dimensionId);
    }
}
