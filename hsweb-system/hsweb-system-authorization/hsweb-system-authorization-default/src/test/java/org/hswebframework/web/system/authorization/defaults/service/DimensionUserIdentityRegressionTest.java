package org.hswebframework.web.system.authorization.defaults.service;

import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.system.authorization.api.entity.DimensionUserEntity;
import org.hswebframework.web.system.authorization.api.event.DimensionBindEvent;
import org.hswebframework.web.system.authorization.api.event.DimensionUnbindEvent;
import org.hswebframework.web.system.authorization.defaults.service.reactive.ReactiveTestApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.*;

/** 真实数据库与绑定事件链保持旧 ID 的保存、查询及解绑行为。 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ReactiveTestApplication.class, DimensionUserIdentityRegressionTest.Configuration.class}, properties = {
    "spring.r2dbc.generate-unique-name=true",
    "spring.r2dbc.name=DimensionUserIdentityRegressionTest"
})
public class DimensionUserIdentityRegressionTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Autowired
    private DefaultDimensionUserService service;

    @Autowired
    private BindingEvents events;

    @Test
    public void generatedLegacyIdSurvivesSaveQueryAndUnbind() {
        DimensionUserEntity member = member();
        member.generateId();
        verifySaveQueryAndUnbind(member);
    }

    @Test
    public void providedIdSurvivesSaveQueryAndUnbind() {
        DimensionUserEntity member = member();
        member.setId("public-" + IDGenerator.MD5.generate());
        verifySaveQueryAndUnbind(member);
    }

    private void verifySaveQueryAndUnbind(DimensionUserEntity member) {
        String expectedId = member.getId();
        service.save(member).then(findTuple(member)).as(StepVerifier::create).assertNext(stored -> {
            assertEquals(expectedId, stored.getId());
            assertEquals("dimension", stored.getDimensionName());
            assertEquals("user", stored.getUserName());
            assertEquals("relation-before", stored.getRelationName());
        }).expectComplete().verify(TIMEOUT);
        assertEquals(1, events.bound.stream().filter(event -> matches(member, event.getType(),
            event.getDimensionId(), event.getUserId())).count());

        member.setRelationName("relation-after");
        service.save(member).then(service.findById(expectedId)).as(StepVerifier::create).assertNext(stored -> {
            assertEquals(expectedId, stored.getId());
            assertEquals(member.getDimensionTypeId(), stored.getDimensionTypeId());
            assertEquals(member.getDimensionId(), stored.getDimensionId());
            assertEquals(member.getUserId(), stored.getUserId());
            assertEquals("relation-after", stored.getRelationName());
        }).expectComplete().verify(TIMEOUT);
        assertEquals(2, events.bound.stream().filter(event -> matches(member, event.getType(),
            event.getDimensionId(), event.getUserId())).count());
        tupleCount(member).as(StepVerifier::create).expectNext(1).expectComplete().verify(TIMEOUT);

        service.deleteById(expectedId).as(StepVerifier::create).expectNext(1).expectComplete().verify(TIMEOUT);
        assertEquals(1, events.unbound.stream().filter(event -> matches(member, event.getType(),
            event.getDimensionId(), event.getUserId())).count());
        service.findById(expectedId).as(StepVerifier::create).expectComplete().verify(TIMEOUT);
        tupleCount(member).as(StepVerifier::create).expectNext(0).expectComplete().verify(TIMEOUT);
    }

    private static boolean matches(DimensionUserEntity member, String type, String dimension, List<String> users) {
        return member.getDimensionTypeId().equals(type) && member.getDimensionId().equals(dimension)
            && users.equals(List.of(member.getUserId()));
    }

    private Mono<DimensionUserEntity> findTuple(DimensionUserEntity member) {
        return service.createQuery().where(DimensionUserEntity::getDimensionTypeId, member.getDimensionTypeId())
            .and(DimensionUserEntity::getDimensionId, member.getDimensionId())
            .and(DimensionUserEntity::getUserId, member.getUserId()).fetchOne();
    }

    private Mono<Integer> tupleCount(DimensionUserEntity member) {
        return service.createQuery().where(DimensionUserEntity::getDimensionTypeId, member.getDimensionTypeId())
            .and(DimensionUserEntity::getDimensionId, member.getDimensionId())
            .and(DimensionUserEntity::getUserId, member.getUserId()).count();
    }

    private static DimensionUserEntity member() {
        String token = IDGenerator.MD5.generate();
        DimensionUserEntity member = new DimensionUserEntity();
        member.setDimensionTypeId("role");
        member.setDimensionId("dimension-" + token);
        member.setDimensionName("dimension");
        member.setUserId("user-" + token);
        member.setUserName("user");
        member.setRelationName("relation-before");
        return member;
    }

    static class BindingEvents {
        final List<DimensionBindEvent> bound = new CopyOnWriteArrayList<>();
        final List<DimensionUnbindEvent> unbound = new CopyOnWriteArrayList<>();

        @EventListener
        public void bound(DimensionBindEvent event) {
            bound.add(event);
        }

        @EventListener
        public void unbound(DimensionUnbindEvent event) {
            unbound.add(event);
        }
    }

    @TestConfiguration
    static class Configuration {
        @Bean
        BindingEvents bindingEvents() {
            return new BindingEvents();
        }
    }
}
