package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.DimensionProvider;
import org.hswebframework.web.authorization.dimension.DimensionManager;
import org.hswebframework.web.authorization.dimension.DimensionUserBind;
import org.hswebframework.web.authorization.dimension.DimensionUserBindProvider;
import org.hswebframework.web.authorization.dimension.DimensionUserDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultDimensionManager implements DimensionManager {

    private final List<DimensionProvider> dimensionProviders = new CopyOnWriteArrayList<>();
    private final List<DimensionUserBindProvider> bindProviders = new CopyOnWriteArrayList<>();

    private final Mono<Map<String, DimensionProvider>> providerMapping = Flux
            .defer(() -> Flux.fromIterable(dimensionProviders))
            .flatMap(provider -> provider
                    .getAllType()
                    .map(type -> Tuples.of(type.getId(), provider)))
            .collectMap(Tuple2::getT1, Tuple2::getT2);

    public DefaultDimensionManager() {

    }

    public void addProvider(DimensionProvider provider) {
        dimensionProviders.add(provider);
    }

    public void addBindProvider(DimensionUserBindProvider bindProvider) {
        bindProviders.add(bindProvider);
    }

    private Mono<Map<String, DimensionProvider>> providerMapping() {
        return providerMapping;
    }

    @Override
    public Flux<DimensionUserDetail> getUserDimension(Collection<String> userId) {
        return this
                .providerMapping()
                .flatMapMany(providerMapping -> Flux
                        .fromIterable(bindProviders)
                        //获取绑定信息
                        .flatMap(provider -> provider.getDimensionBindInfo(userId))
                        .groupBy(DimensionUserBind::getDimensionType)
                        .flatMap(group -> {
                            String type = group.key();
                            Flux<DimensionUserBind> binds = group.cache();
                            DimensionProvider provider = providerMapping.get(type);
                            if (null == provider) {
                                return Mono.empty();
                            }
                            //获取维度信息
                            return binds
                                    .map(DimensionUserBind::getDimensionId)
                                    .collect(Collectors.toSet())
                                    .flatMapMany(idList -> provider.getDimensionsById(SimpleDimensionType.of(type), idList))
                                    .collectMap(Dimension::getId, Function.identity())
                                    .flatMapMany(mapping -> binds
                                            .groupBy(DimensionUserBind::getUserId)
                                            .flatMap(userGroup -> Mono
                                                    .zip(
                                                            Mono.just(userGroup.key()),
                                                            userGroup
                                                                    .<Dimension>handle((bind, sink) -> {
                                                                        Dimension dimension = mapping.get(bind.getDimensionId());
                                                                        if (dimension != null) {
                                                                            sink.next(dimension);
                                                                        }
                                                                    })
                                                                    .collectList(),
                                                            DimensionUserDetail::of
                                                    ))
                                    );
                        })
                        )
                .groupBy(DimensionUserDetail::getUserId)
                .flatMap(group->group.reduce(DimensionUserDetail::merge));
    }
}
