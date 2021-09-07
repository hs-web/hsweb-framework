package org.hswebframework.web.authorization.dimension;

import reactor.core.publisher.Flux;

import java.util.Collection;

public interface DimensionUserBindProvider {

    Flux<DimensionUserBind> getDimensionBindInfo(Collection<String> userIdList);

}
