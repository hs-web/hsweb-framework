package org.hswebframework.web.authorization.basic.aop;

import org.hswebframework.ezorm.core.param.Param;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.web.authorization.*;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.authorization.simple.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;
import java.util.function.Function;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class AopAuthorizingControllerTest {

    @Autowired
    public TestController testController;

    @Test
    public void testAccessDeny() {

        SimpleAuthentication authentication = new SimpleAuthentication();

        authentication.setUser(SimpleUser.builder().id("test").username("test").build());
//        authentication.setPermissions(Arrays.asList(SimplePermission.builder().id("test").build()));
        authentication.setPermissions(Collections.emptyList());
        ReactiveAuthenticationHolder.setSupplier(new ReactiveAuthenticationSupplier() {
            @Override
            public Mono<Authentication> get(String userId) {
                return Mono.empty();
            }

            @Override
            public Mono<Authentication> get() {
                return Mono.just(authentication);
            }
        });

        testController.getUser()
                .map(User::getId)
                .onErrorReturn(AccessDenyException.class, "403")
                .as(StepVerifier::create)
                .expectNext("403")
                .verifyComplete();

        testController.getUserAfter()
                .map(User::getId)
                .onErrorReturn(AccessDenyException.class, "403")
                .as(StepVerifier::create)
                .expectNext("403")
                .verifyComplete();
    }

    @Test
    public void testFiledDeny() {
        SimpleAuthentication authentication = new SimpleAuthentication();

        SimpleFieldFilterDataAccessConfig config = new SimpleFieldFilterDataAccessConfig();
        config.setAction("query");
        config.setFields(new HashSet<>(Arrays.asList("name")));

        authentication.setUser(SimpleUser.builder().id("test").username("test").build());
        authentication.setPermissions(Arrays.asList(SimplePermission.builder()
                .actions(Collections.singleton("query"))
                .dataAccesses(Collections.singleton(config))
                .id("test").build()));

        ReactiveAuthenticationHolder.setSupplier(new ReactiveAuthenticationSupplier() {
            @Override
            public Mono<Authentication> get(String userId) {
                return Mono.empty();
            }

            @Override
            public Mono<Authentication> get() {
                return Mono.just(authentication);
            }
        });

        testController.queryUser(new QueryParam())
                .map(Param::getExcludes)
                .as(StepVerifier::create)
                .expectNextMatches(f -> f.contains("name"))
                .verifyComplete();

        testController.queryUser(Mono.just(new QueryParam()))
                .map(Param::getExcludes)
                .as(StepVerifier::create)
                .expectNextMatches(f -> f.contains("name"))
                .verifyComplete();
    }

    @Test
    public void testDimensionDataAccess() {
        SimpleAuthentication authentication = new SimpleAuthentication();

        DimensionDataAccessConfig config = new DimensionDataAccessConfig();
        config.setAction("query");
        config.setScopeType("role");

        DimensionDataAccessConfig config2 = new DimensionDataAccessConfig();
        config2.setAction("save");
        config2.setScopeType("role");
        ReactiveAuthenticationHolder.setSupplier(new ReactiveAuthenticationSupplier() {
            @Override
            public Mono<Authentication> get(String userId) {
                return Mono.empty();
            }

            @Override
            public Mono<Authentication> get() {
                return Mono.just(authentication);
            }
        });

        authentication.setUser(SimpleUser.builder().id("test").username("test").build());
        authentication.setPermissions(Arrays.asList(SimplePermission.builder()
                .actions(new HashSet<>(Arrays.asList("query", "save")))
                .dataAccesses(new HashSet<>(Arrays.asList(config, config2)))
                .id("test").build()));
        authentication.setDimensions(Collections.singletonList(Dimension.of("test", "test", DefaultDimensionType.role)));

        testController.queryUserByDimension(Mono.just(new QueryParam()))
                .map(Param::getTerms)
                .flatMapIterable(Function.identity())
                .next()
                .map(Term::getValue)
                .<Collection<Object>>map(Collection.class::cast)
                .flatMapIterable(Function.identity())
                .next()
                .as(StepVerifier::create)
                .expectNextMatches("test"::equals)
                .verifyComplete();

        TestEntity testEntity = new TestEntity();
        testEntity.setRoleId("123");

        testController.save(Mono.just(testEntity))
                .as(StepVerifier::create)
                .expectError(AccessDenyException.class)
                .verify();

        testController.add(Mono.just(testEntity))
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        testController.update(testEntity.getId(),Mono.just(testEntity))
                .as(StepVerifier::create)
                .expectError(AccessDenyException.class)
                .verify();

        testEntity = new TestEntity();
        testEntity.setRoleId("test");

        testController.save(Mono.just(testEntity))
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();


    }
}