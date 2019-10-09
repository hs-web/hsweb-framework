package org.hswebframework.web.system.authorization.api;

import reactor.core.publisher.Flux;

public interface PermissionDimensionProvider {

    String getDimension();

    String getName();

    Flux<PermissionDimension> getDimensionByUserId(String userId);

    Flux<String> getUserIdByDimensionId(String dimensionId);

}
