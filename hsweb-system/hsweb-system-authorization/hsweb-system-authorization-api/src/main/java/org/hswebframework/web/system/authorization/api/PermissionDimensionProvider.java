package org.hswebframework.web.system.authorization.api;

import reactor.core.publisher.Flux;

public interface PermissionDimensionProvider {

    Flux<PermissionDimension> getDimensionByUserId(String userId);

    Flux<String> getUserIdByDimensionId(String dimensionId);

}
