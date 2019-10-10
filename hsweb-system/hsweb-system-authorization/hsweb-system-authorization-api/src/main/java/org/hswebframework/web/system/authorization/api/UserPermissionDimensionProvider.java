package org.hswebframework.web.system.authorization.api;

import reactor.core.publisher.Flux;

public class UserPermissionDimensionProvider implements PermissionDimensionProvider {
    @Override
    public String getDimension() {
        return "user";
    }

    @Override
    public String getName() {
        return "用户";
    }

    @Override
    public Flux<PermissionDimension> getDimensionByUserId(String userId) {
        return Flux.just(userId)
                .map(PermissionDimension::of);
    }

    @Override
    public Flux<String> getUserIdByDimensionId(String dimensionId) {
        return Flux.just(dimensionId);
    }
}
