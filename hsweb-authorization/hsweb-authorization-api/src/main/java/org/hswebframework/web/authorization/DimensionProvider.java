package org.hswebframework.web.authorization;

import reactor.core.publisher.Flux;

public interface DimensionProvider {

    Flux<DimensionType> getAllType();

    Flux<Dimension> getDimensionByUserId(String userId);

    Flux<String> getUserIdByDimensionId(String dimensionId);

}
