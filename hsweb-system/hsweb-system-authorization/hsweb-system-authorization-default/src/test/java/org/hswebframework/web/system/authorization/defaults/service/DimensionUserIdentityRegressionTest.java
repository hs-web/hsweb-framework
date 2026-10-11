package org.hswebframework.web.system.authorization.defaults.service;

import org.hswebframework.ezorm.rdb.exception.DuplicateKeyException;
import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.crud.events.EntityBeforeQueryEvent;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.system.authorization.api.entity.DimensionUserEntity;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/** 正式 ORM 写入事件统一确定成员身份，兼容旧公开 ID 和所有 service/repository 入口。 */
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
    private QueryCounter queries;

    @Test
    public void allEntryPointsStoreTheGeneratedTupleIdBeforeSqlExecution() {
        for (Write entry : Write.values()) {
            DimensionUserEntity input = uniqueMember();
            DimensionUserEntity expected = member(input.getDimensionTypeId(), input.getDimensionId(), input.getUserId());
            expected.generateId();
            write(entry, input).then(findTuple(input)).as(StepVerifier::create).assertNext(stored -> {
                assertEquals(entry.name(), expected.getId(), stored.getId());
                assertEquals("dimension", stored.getDimensionName());
                assertEquals("user", stored.getUserName());
            }).expectComplete().verify(TIMEOUT);
        }
    }

    @Test
    public void everySaveEntryPointReusesLegacyIdForEmptyAndGeneratedInputIds() {
        for (Write entry : Write.values()) {
            if (entry.create) {
                continue;
            }
            for (boolean preGenerated : List.of(false, true)) {
                DimensionUserEntity legacy = seedLegacyMember();
                DimensionUserEntity input = member(legacy.getDimensionTypeId(), legacy.getDimensionId(), legacy.getUserId());
                input.setRelationName("relation-after");
                if (preGenerated) {
                    input.generateId();
                }
                write(entry, input).then(findTuple(input)).as(StepVerifier::create).assertNext(stored -> {
                    assertEquals(entry.name(), legacy.getId(), stored.getId());
                    assertEquals("relation-after", stored.getRelationName());
                }).expectComplete().verify(TIMEOUT);
                tupleCount(input).as(StepVerifier::create).expectNext(1).verifyComplete();
            }
        }
    }

    @Test
    public void everyCreateEntryPointStillRejectsAnExistingTuple() {
        for (Write entry : Write.values()) {
            if (!entry.create) {
                continue;
            }
            DimensionUserEntity legacy = seedLegacyMember();
            DimensionUserEntity duplicate = member(legacy.getDimensionTypeId(), legacy.getDimensionId(), legacy.getUserId());
            duplicate.setRelationName("duplicate-must-not-update");
            write(entry, duplicate).as(StepVerifier::create).expectError(DuplicateKeyException.class).verify(TIMEOUT);
            findTuple(legacy).as(StepVerifier::create).assertNext(stored -> {
                assertEquals(legacy.getId(), stored.getId());
                assertEquals("relation-before", stored.getRelationName());
            }).expectComplete().verify(TIMEOUT);
            tupleCount(legacy).as(StepVerifier::create).expectNext(1).verifyComplete();
        }
    }

    @Test
    public void differentDelimiterContainingTuplesDoNotOverwriteEachOther() {
        String token = IDGenerator.MD5.generate().substring(0, 12);
        String user = "user-" + token;
        DimensionUserEntity first = member(token + "-team", "district", user);
        DimensionUserEntity second = member(token, "team-district", user);
        first.generateId();
        second.generateId();
        service.save(first).then(service.save(second)).then(service.createQuery()
            .where(DimensionUserEntity::getUserId, user).fetch().collectMap(DimensionUserEntity::getDimensionTypeId))
            .as(StepVerifier::create).assertNext(rows -> {
                assertEquals(2, rows.size());
                assertEquals("district", rows.get(first.getDimensionTypeId()).getDimensionId());
                assertEquals("team-district", rows.get(second.getDimensionTypeId()).getDimensionId());
                assertNotEquals(rows.get(first.getDimensionTypeId()).getId(), rows.get(second.getDimensionTypeId()).getId());
            }).expectComplete().verify(TIMEOUT);
    }

    @Test
    public void everyEntryPointRejectsExplicitIdForAnotherTupleBeforeNaturalKeyResolution() {
        for (Write entry : Write.values()) {
            DimensionUserEntity first = seedLegacyMember();
            DimensionUserEntity second = seedLegacyMember();
            DimensionUserEntity attack = member(second.getDimensionTypeId(), second.getDimensionId(), second.getUserId());
            attack.setId(first.getId());
            attack.setRelationName("must-not-overwrite");
            write(entry, attack).as(StepVerifier::create).expectError(BusinessException.class).verify(TIMEOUT);
            service.findById(List.of(first.getId(), second.getId())).collectMap(DimensionUserEntity::getId)
                .as(StepVerifier::create).assertNext(rows -> {
                    assertEquals(2, rows.size());
                    assertEquals(first.getDimensionTypeId(), rows.get(first.getId()).getDimensionTypeId());
                    assertEquals(second.getDimensionTypeId(), rows.get(second.getId()).getDimensionTypeId());
                    assertEquals("relation-before", rows.get(first.getId()).getRelationName());
                    assertEquals("relation-before", rows.get(second.getId()).getRelationName());
                }).expectComplete().verify(TIMEOUT);
            tupleCount(second).as(StepVerifier::create).expectNext(1).verifyComplete();
        }
    }

    @Test
    public void implicitGeneratedIdCannotOverwriteAnotherTuplesCallerProvidedId() {
        for (Write entry : Write.values()) {
            DimensionUserEntity input = uniqueMember();
            DimensionUserEntity candidate = member(input.getDimensionTypeId(), input.getDimensionId(), input.getUserId());
            candidate.generateId();
            DimensionUserEntity occupied = uniqueMember();
            occupied.setId(candidate.getId());
            service.getRepository().insert(Mono.just(occupied)).as(StepVerifier::create).expectNext(1).verifyComplete();
            assertNull(input.getId());
            write(entry, input).as(StepVerifier::create).expectError(BusinessException.class).verify(TIMEOUT);
            service.findById(occupied.getId()).as(StepVerifier::create).assertNext(stored -> {
                assertEquals(occupied.getDimensionTypeId(), stored.getDimensionTypeId());
                assertEquals(occupied.getDimensionId(), stored.getDimensionId());
                assertEquals(occupied.getUserId(), stored.getUserId());
                assertEquals("relation-before", stored.getRelationName());
            }).expectComplete().verify(TIMEOUT);
            tupleCount(input).as(StepVerifier::create).expectNext(0).verifyComplete();
        }
    }

    @Test
    public void differentTuplesCannotResolveToOnePublicIdWithinACollection() {
        verifyBatchIdentityConflict(false);
    }

    @Test
    public void differentTuplesCannotResolveToOnePublicIdAcrossQueryBuffers() {
        verifyBatchIdentityConflict(true);
    }

    @Test
    public void legacyNaturalTupleTakesPriorityOverAnOccupiedImplicitCandidateId() {
        for (Write entry : Write.values()) {
            if (entry.create) {
                continue;
            }
            DimensionUserEntity legacy = seedLegacyMember();
            DimensionUserEntity candidate = member(legacy.getDimensionTypeId(), legacy.getDimensionId(), legacy.getUserId());
            candidate.generateId();
            DimensionUserEntity occupied = uniqueMember();
            occupied.setId(candidate.getId());
            service.getRepository().insert(Mono.just(occupied)).as(StepVerifier::create).expectNext(1).verifyComplete();
            DimensionUserEntity input = member(legacy.getDimensionTypeId(), legacy.getDimensionId(), legacy.getUserId());
            input.setRelationName("legacy-rebind-after");
            write(entry, input).then(findTuple(legacy)).as(StepVerifier::create).assertNext(stored -> {
                assertEquals(legacy.getId(), stored.getId());
                assertEquals("legacy-rebind-after", stored.getRelationName());
            }).expectComplete().verify(TIMEOUT);
            service.findById(occupied.getId()).as(StepVerifier::create).assertNext(stored -> {
                assertEquals(occupied.getDimensionTypeId(), stored.getDimensionTypeId());
                assertEquals(occupied.getDimensionId(), stored.getDimensionId());
                assertEquals(occupied.getUserId(), stored.getUserId());
                assertEquals("relation-before", stored.getRelationName());
            }).expectComplete().verify(TIMEOUT);
            tupleCount(legacy).as(StepVerifier::create).expectNext(1).verifyComplete();
            tupleCount(occupied).as(StepVerifier::create).expectNext(1).verifyComplete();
        }
    }

    @Test
    public void incompleteNewTupleCannotResolveToAnExistingMember() {
        DimensionUserEntity legacy = seedLegacyMember();
        for (Write entry : Write.values()) {
            DimensionUserEntity incomplete = new DimensionUserEntity();
            incomplete.setUserId(legacy.getUserId());
            incomplete.setDimensionName("dimension");
            incomplete.setUserName("user");
            incomplete.setRelationName("must-not-overwrite");
            write(entry, incomplete).as(StepVerifier::create).expectError().verify(TIMEOUT);
            service.findById(legacy.getId()).as(StepVerifier::create).assertNext(stored -> {
                assertEquals(legacy.getDimensionTypeId(), stored.getDimensionTypeId());
                assertEquals(legacy.getDimensionId(), stored.getDimensionId());
                assertEquals("relation-before", stored.getRelationName());
            }).expectComplete().verify(TIMEOUT);
            tupleCount(legacy).as(StepVerifier::create).expectNext(1).verifyComplete();
        }
    }

    @Test
    public void bulkServiceAndRepositorySavesKeepExactTupleIdentityWithBoundedReads() {
        for (boolean rawRepository : List.of(false, true)) {
            String token = IDGenerator.MD5.generate().substring(0, 12);
            DimensionUserEntity first = member(token, "first", "first-user-" + token);
            first.setId("legacy-first-" + token);
            DimensionUserEntity second = member(token + "-other", "second", "second-user-" + token);
            second.setId("legacy-second-" + token);
            service.getRepository().insertBatch(Mono.just(List.of(first, second)))
                .as(StepVerifier::create).expectNext(2).verifyComplete();
            DimensionUserEntity firstPatch = member(first.getDimensionTypeId(), first.getDimensionId(), first.getUserId());
            firstPatch.setRelationName("bulk-first-after");
            DimensionUserEntity secondPatch = member(second.getDimensionTypeId(), second.getDimensionId(), second.getUserId());
            secondPatch.generateId();
            secondPatch.setRelationName("bulk-second-after");
            DimensionUserEntity crossTuple = member(first.getDimensionTypeId(), second.getDimensionId(), first.getUserId());
            List<DimensionUserEntity> members = new ArrayList<>(List.of(firstPatch, secondPatch, crossTuple));
            for (int i = 0; i < 202; i++) {
                members.add(member(token, "bulk-" + i, "bulk-user-" + token + "-" + i));
            }
            queries.count.set(0);
            Mono<?> operation = rawRepository ? service.getRepository().save(members) : service.save(members);
            operation.then().as(StepVerifier::create).expectComplete().verify(TIMEOUT);
            assertTrue("205 member writes must not perform one identity SELECT per member", queries.count.get() <= 12);
            findTuple(firstPatch).as(StepVerifier::create).assertNext(stored -> {
                assertEquals(first.getId(), stored.getId());
                assertEquals("bulk-first-after", stored.getRelationName());
            }).expectComplete().verify(TIMEOUT);
            findTuple(secondPatch).as(StepVerifier::create).assertNext(stored -> {
                assertEquals(second.getId(), stored.getId());
                assertEquals("bulk-second-after", stored.getRelationName());
            }).expectComplete().verify(TIMEOUT);
            findTuple(crossTuple).as(StepVerifier::create).assertNext(stored -> {
                assertNotEquals(first.getId(), stored.getId());
                assertNotEquals(second.getId(), stored.getId());
                assertEquals(first.getDimensionTypeId(), stored.getDimensionTypeId());
                assertEquals(second.getDimensionId(), stored.getDimensionId());
            }).expectComplete().verify(TIMEOUT);
            service.createQuery().in(DimensionUserEntity::getDimensionTypeId,
                List.of(first.getDimensionTypeId(), second.getDimensionTypeId())).count()
                .as(StepVerifier::create).expectNext(205).verifyComplete();
        }
    }

    @Test
    public void everyEntryPointKeepsAnUnusedCallerProvidedPublicId() {
        for (Write entry : Write.values()) {
            DimensionUserEntity member = uniqueMember();
            member.setId("public-" + IDGenerator.MD5.generate());
            String expectedId = member.getId();
            write(entry, member).then(findTuple(member)).as(StepVerifier::create).assertNext(stored ->
                assertEquals(entry.name(), expectedId, stored.getId())).expectComplete().verify(TIMEOUT);
        }
    }

    private void verifyBatchIdentityConflict(boolean acrossBuffer) {
        for (boolean rawRepository : List.of(false, true)) {
            DimensionUserEntity first = uniqueMember();
            DimensionUserEntity candidate = member(first.getDimensionTypeId(), first.getDimensionId(), first.getUserId());
            candidate.generateId();
            DimensionUserEntity second = uniqueMember();
            second.setId(candidate.getId());
            List<DimensionUserEntity> inputs = new ArrayList<>();
            inputs.add(first);
            if (acrossBuffer) {
                for (int i = 0; i < 202; i++) {
                    inputs.add(uniqueMember());
                }
            }
            inputs.add(second);
            Mono<?> operation = rawRepository ? service.getRepository().save(inputs) : service.save(inputs);
            operation.then().as(StepVerifier::create).expectError(BusinessException.class).verify(TIMEOUT);
            service.createQuery().in(DimensionUserEntity::getUserId,
                inputs.stream().map(DimensionUserEntity::getUserId).collect(java.util.stream.Collectors.toList()))
                .count().as(StepVerifier::create).expectNext(0).verifyComplete();
            assertEquals("relation-before", first.getRelationName());
            assertEquals("relation-before", second.getRelationName());
        }
    }

    private DimensionUserEntity seedLegacyMember() {
        DimensionUserEntity legacy = uniqueMember();
        legacy.setId("legacy-" + IDGenerator.MD5.generate());
        service.getRepository().insert(Mono.just(legacy)).as(StepVerifier::create).expectNext(1).verifyComplete();
        return legacy;
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

    private Mono<Void> write(Write entry, DimensionUserEntity member) {
        switch (entry) {
            case SERVICE_SAVE_ENTITY: return service.save(member).then();
            case SERVICE_SAVE_COLLECTION: return service.save(List.of(member)).then();
            case SERVICE_SAVE_PUBLISHER: return service.save(Mono.just(member)).then();
            case REPOSITORY_SAVE_ENTITY: return service.getRepository().save(member).then();
            case REPOSITORY_SAVE_COLLECTION: return service.getRepository().save(List.of(member)).then();
            case REPOSITORY_SAVE_PUBLISHER: return service.getRepository().save(Mono.just(member)).then();
            case SERVICE_INSERT_ENTITY: return service.insert(member).then();
            case SERVICE_INSERT_PUBLISHER: return service.insert(Mono.just(member)).then();
            case SERVICE_INSERT_BATCH: return service.insertBatch(Mono.just(List.of(member))).then();
            case REPOSITORY_INSERT_PUBLISHER: return service.getRepository().insert(Mono.just(member)).then();
            case REPOSITORY_INSERT_BATCH: return service.getRepository().insertBatch(Mono.just(List.of(member))).then();
            default: throw new AssertionError(entry);
        }
    }

    private static DimensionUserEntity uniqueMember() {
        String token = IDGenerator.MD5.generate();
        return member(token.substring(0, 16), "dimension-" + token, "user-" + token);
    }

    private static DimensionUserEntity member(String type, String dimension, String user) {
        DimensionUserEntity member = new DimensionUserEntity();
        member.setDimensionTypeId(type);
        member.setDimensionId(dimension);
        member.setDimensionName("dimension");
        member.setUserId(user);
        member.setUserName("user");
        member.setRelationName("relation-before");
        return member;
    }

    static class QueryCounter {
        final AtomicInteger count = new AtomicInteger();

        @EventListener
        public void countQuery(EntityBeforeQueryEvent<DimensionUserEntity> event) {
            count.incrementAndGet();
        }
    }

    @TestConfiguration
    static class Configuration {
        @Bean
        QueryCounter queryCounter() {
            return new QueryCounter();
        }
    }

    private enum Write {
        SERVICE_SAVE_ENTITY(false), SERVICE_SAVE_COLLECTION(false), SERVICE_SAVE_PUBLISHER(false),
        REPOSITORY_SAVE_ENTITY(false), REPOSITORY_SAVE_COLLECTION(false), REPOSITORY_SAVE_PUBLISHER(false),
        SERVICE_INSERT_ENTITY(true), SERVICE_INSERT_PUBLISHER(true), SERVICE_INSERT_BATCH(true),
        REPOSITORY_INSERT_PUBLISHER(true), REPOSITORY_INSERT_BATCH(true);

        final boolean create;

        Write(boolean create) {
            this.create = create;
        }
    }
}
