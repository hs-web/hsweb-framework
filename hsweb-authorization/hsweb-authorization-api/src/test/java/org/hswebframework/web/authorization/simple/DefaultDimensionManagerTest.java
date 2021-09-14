package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.DimensionProvider;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.authorization.dimension.DimensionUserBind;
import org.hswebframework.web.authorization.dimension.DimensionUserBindProvider;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;

public class DefaultDimensionManagerTest {

    @Test
    public void test() {
        DefaultDimensionManager manager = new DefaultDimensionManager();
        manager.addBindProvider(userIdList -> Flux.just(
                DimensionUserBind.of("testUser", "testType", "testId")
                , DimensionUserBind.of("testUser", "testType", "testId2")));
        manager.addProvider(new DimensionProvider() {
            @Override
            public Flux<? extends DimensionType> getAllType() {
                return Flux.just(SimpleDimensionType.of("testType"));
            }

            @Override
            public Flux<? extends Dimension> getDimensionsById(DimensionType type,
                                                               Collection<String> idList) {
                return Flux.just(SimpleDimension.of("testId", "testName", SimpleDimensionType.of("testType"), null));
            }

            @Override
            public Flux<? extends Dimension> getDimensionByUserId(String userId) {
                return Flux.empty();
            }

            @Override
            public Mono<? extends Dimension> getDimensionById(DimensionType type, String id) {
                return Mono.empty();
            }

            @Override
            public Flux<String> getUserIdByDimensionId(String dimensionId) {
                return Flux.empty();
            }
        });

        manager.getUserDimension(Collections.singleton("testUser"))
               .as(StepVerifier::create)
               .expectNextMatches(detail -> detail.getDimensions().size() == 1)
               .verifyComplete();
    }
}