package org.hswebframework.web.datasource;

import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Mono;

public interface R2dbcDataSource extends DynamicDataSource<Mono<ConnectionFactory>> {
    @Override
    Mono<ConnectionFactory> getNative();
}
