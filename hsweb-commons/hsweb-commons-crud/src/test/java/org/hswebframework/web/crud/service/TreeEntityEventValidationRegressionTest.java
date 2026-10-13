package org.hswebframework.web.crud.service;

import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.crud.TestApplication;
import org.hswebframework.web.crud.entity.TestTreeSortEntity;
import org.hswebframework.web.crud.events.EntityBeforeCreateEvent;
import org.hswebframework.web.crud.events.EntityBeforeModifyEvent;
import org.hswebframework.web.crud.events.EntityBeforeSaveEvent;
import org.hswebframework.web.id.IDGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/** 正式实体事件共用业务规则，纯 UPDATE 及显式 child 拒绝保持完整原事务回滚。 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApplication.class, TreeEntityEventValidationRegressionTest.Configuration.class},
    properties = {
        "spring.r2dbc.generate-unique-name=true",
        "spring.r2dbc.name=TreeEntityEventValidationRegressionTest"
    })
public class TreeEntityEventValidationRegressionTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String REJECTION = "business validation";

    @Autowired private ValidatingTreeService service;
    @Autowired private BusinessValidationListener listener;

    @Test
    public void monoTargetRejectionUsesCompleteModifyAfter() {
        verifyTargetRejection(false);
    }

    @Test
    public void entityTargetRejectionUsesCompleteModifyAfter() {
        verifyTargetRejection(true);
    }

    @Test
    public void monoExistingChildRejectionRollsBackWrittenParent() {
        verifyChildRejection(false, false);
    }

    @Test
    public void entityExistingChildRejectionRollsBackWrittenParent() {
        verifyChildRejection(true, false);
    }

    @Test
    public void monoNewChildRejectionRollsBackWrittenParent() {
        verifyChildRejection(false, true);
    }

    @Test
    public void entityNewChildRejectionRollsBackWrittenParent() {
        verifyChildRejection(true, true);
    }

    @Test
    public void saveRejectsThroughTheSameBusinessValidation() {
        TestTreeSortEntity rejected = node(IDGenerator.MD5.generate(), "forbidden-save");
        reset();
        expectRejection(service.save(rejected));
        assertEquals(rejected.getName(), listener.getBeforeSave(rejected.getId()).getName());
        service.findById(rejected.getId()).as(StepVerifier::create).verifyComplete();
    }

    @Test
    public void insertRejectsThroughTheSameBusinessValidation() {
        TestTreeSortEntity rejected = node(IDGenerator.MD5.generate(), "forbidden-create");
        reset();
        expectRejection(service.insert(rejected));
        assertEquals(rejected.getName(), listener.getBeforeCreate(rejected.getId()).getName());
        service.findById(rejected.getId()).as(StepVerifier::create).verifyComplete();
    }

    private void verifyTargetRejection(boolean entityOverload) {
        List<TestTreeSortEntity> tree = savedTree();
        TestTreeSortEntity target = tree.get(1);
        Map<String, List<Object>> before = snapshot(target.getGroupId());
        reset();
        TestTreeSortEntity patch = patch("forbidden-target");

        expectRejection(update(target.getId(), patch, entityOverload));

        TestTreeSortEntity afterInput = listener.getBeforeModify(target.getId());
        assertCompleteExistingAfter(target, "forbidden-target", afterInput);
        assertEquals(before, snapshot(target.getGroupId()));
        assertNull("existing UPDATE must not invoke Save", listener.getBeforeSave(target.getId()));
    }

    private void verifyChildRejection(boolean entityOverload, boolean newChild) {
        List<TestTreeSortEntity> tree = savedTree();
        TestTreeSortEntity target = tree.get(1);
        TestTreeSortEntity child = tree.get(2);
        Map<String, List<Object>> before = snapshot(target.getGroupId());
        reset();
        service.expectWrittenParent(target.getId());
        TestTreeSortEntity patch = patch("root-after");
        TestTreeSortEntity childPatch = patch(newChild ? "forbidden-new-child" : "forbidden-existing-child");
        if (newChild) {
            childPatch.setGroupId(target.getGroupId());
            childPatch.setDefaultTest("business-before");
        } else {
            childPatch.setId(child.getId());
        }
        patch.setChildren(List.of(childPatch));

        expectRejection(update(target.getId(), patch, entityOverload));

        TestTreeSortEntity writtenParent = service.getParentAtRejection();
        assertNotNull("child validation must read the preceding real parent UPDATE", writtenParent);
        assertEquals(target.getId(), writtenParent.getId());
        assertEquals("root-after", writtenParent.getName());
        assertEquals(target.getPath(), writtenParent.getPath());
        assertCompleteExistingAfter(target, "root-after", listener.getBeforeModify(target.getId()));
        if (newChild) {
            assertNotNull(childPatch.getId());
            TestTreeSortEntity saveInput = listener.getBeforeSave(childPatch.getId());
            assertNotNull("explicit new child must use the formal Save boundary", saveInput);
            assertEquals("forbidden-new-child", saveInput.getName());
            assertEquals("business-before", saveInput.getDefaultTest());
            assertEquals(target.getId(), saveInput.getParentId());
            assertTrue(saveInput.getPath().startsWith(target.getPath() + "-"));
            assertEquals(Integer.valueOf(target.getLevel() + 1), saveInput.getLevel());
            service.findById(childPatch.getId()).as(StepVerifier::create).verifyComplete();
        } else {
            assertCompleteExistingAfter(child, "forbidden-existing-child", listener.getBeforeModify(child.getId()));
            assertNull("existing child UPDATE must not invoke Save", listener.getBeforeSave(child.getId()));
        }
        assertEquals("the original transaction must restore all business and tree columns", before,
            snapshot(target.getGroupId()));
    }

    private List<TestTreeSortEntity> savedTree() {
        String group = IDGenerator.MD5.generate();
        TestTreeSortEntity outer = node(group, "outer-before");
        TestTreeSortEntity root = node(group, "root-before");
        TestTreeSortEntity child = node(group, "child-before");
        TestTreeSortEntity leaf = node(group, "leaf-before");
        outer.setChildren(List.of(root));
        root.setChildren(List.of(child));
        child.setChildren(List.of(leaf));
        service.save(outer).as(StepVerifier::create).expectNextCount(1).verifyComplete();
        return service.findById(List.of(outer.getId(), root.getId(), child.getId(), leaf.getId()))
            .collectMap(TestTreeSortEntity::getId)
            .map(stored -> List.of(stored.get(outer.getId()), stored.get(root.getId()),
                stored.get(child.getId()), stored.get(leaf.getId())))
            .block(TIMEOUT);
    }

    private Map<String, List<Object>> snapshot(String group) {
        return service.getRepository().createQuery().where("groupId", group).fetch()
            .collectMap(TestTreeSortEntity::getId, entity -> Arrays.<Object>asList(entity.getParentId(), entity.getPath(),
                entity.getLevel(), entity.getSortIndex(), entity.getName(), entity.getDefaultTest(), entity.getGroupId()))
            .block(TIMEOUT);
    }

    private Mono<Integer> update(String id, TestTreeSortEntity patch, boolean entityOverload) {
        return entityOverload ? service.updateById(id, patch) : service.updateById(id, Mono.just(patch));
    }

    private void reset() {
        listener.reset();
        service.expectWrittenParent(null);
    }

    private static void expectRejection(Mono<?> operation) {
        operation.as(StepVerifier::create).expectErrorMatches(error ->
            error instanceof IllegalArgumentException && REJECTION.equals(error.getMessage())).verify(TIMEOUT);
    }

    private static void assertCompleteExistingAfter(TestTreeSortEntity previous, String name,
                                                   TestTreeSortEntity afterInput) {
        assertNotNull("formal Modify must provide complete after input", afterInput);
        assertEquals(previous.getId(), afterInput.getId());
        assertEquals(name, afterInput.getName());
        assertEquals(previous.getDefaultTest(), afterInput.getDefaultTest());
        assertEquals(previous.getGroupId(), afterInput.getGroupId());
        assertEquals(previous.getParentId(), afterInput.getParentId());
        assertEquals(previous.getPath(), afterInput.getPath());
        assertEquals(previous.getLevel(), afterInput.getLevel());
        assertEquals(previous.getSortIndex(), afterInput.getSortIndex());
    }

    private static TestTreeSortEntity node(String group, String name) {
        TestTreeSortEntity entity = patch(name);
        entity.setId(IDGenerator.MD5.generate());
        entity.setGroupId(group);
        entity.setDefaultTest("business-before");
        entity.setSortIndex(17L);
        return entity;
    }

    private static TestTreeSortEntity patch(String name) {
        TestTreeSortEntity entity = new TestTreeSortEntity();
        entity.setName(name);
        return entity;
    }

    private static TestTreeSortEntity copy(TestTreeSortEntity entity) {
        return FastBeanCopier.copy(entity, new TestTreeSortEntity());
    }

    /** 业务规则由 Service 持有，正式 Create/Save/Modify 监听器共用同一函数。 */
    public static class ValidatingTreeService extends TestTreeSortEntityService {
        private volatile String expectedParentId;
        private final AtomicReference<TestTreeSortEntity> parentAtRejection = new AtomicReference<>();

        public void expectWrittenParent(String id) {
            expectedParentId = id;
            parentAtRejection.set(null);
        }

        public TestTreeSortEntity getParentAtRejection() {
            return parentAtRejection.get();
        }

        public Mono<Void> validateEntities(List<TestTreeSortEntity> entities) {
            return Flux.fromIterable(entities).concatMap(entity -> {
                if (!entity.getName().startsWith("forbidden-")) {
                    return Mono.empty();
                }
                String parentId = expectedParentId;
                Mono<Void> parentRead = parentId == null ? Mono.empty()
                    : findById(parentId).doOnNext(parent -> parentAtRejection.set(copy(parent)))
                        .switchIfEmpty(Mono.error(new AssertionError("updated parent must exist"))).then();
                return parentRead.then(Mono.error(new IllegalArgumentException(REJECTION)));
            }).then();
        }
    }

    public static class BusinessValidationListener {
        private final ValidatingTreeService service;
        private final Map<String, TestTreeSortEntity> beforeCreate = new ConcurrentHashMap<>();
        private final Map<String, TestTreeSortEntity> beforeSave = new ConcurrentHashMap<>();
        private final Map<String, TestTreeSortEntity> beforeModify = new ConcurrentHashMap<>();

        public BusinessValidationListener(ValidatingTreeService service) {
            this.service = service;
        }

        public void reset() {
            beforeCreate.clear();
            beforeSave.clear();
            beforeModify.clear();
        }

        public TestTreeSortEntity getBeforeCreate(String id) { return beforeCreate.get(id); }
        public TestTreeSortEntity getBeforeSave(String id) { return beforeSave.get(id); }
        public TestTreeSortEntity getBeforeModify(String id) { return beforeModify.get(id); }

        @EventListener
        public void beforeCreate(EntityBeforeCreateEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class) {
                event.getEntity().forEach(entity -> beforeCreate.put(entity.getId(), copy(entity)));
                event.async(service.validateEntities(event.getEntity()));
            }
        }

        @EventListener
        public void beforeSave(EntityBeforeSaveEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class) {
                event.getEntity().forEach(entity -> beforeSave.put(entity.getId(), copy(entity)));
                event.async(service.validateEntities(event.getEntity()));
            }
        }

        @EventListener
        public void beforeModify(EntityBeforeModifyEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class) {
                event.getAfter().forEach(entity -> beforeModify.put(entity.getId(), copy(entity)));
                event.async(service.validateEntities(event.getAfter()));
            }
        }
    }

    @TestConfiguration
    public static class Configuration {
        @Bean public ValidatingTreeService eventValidationTreeService() { return new ValidatingTreeService(); }
        @Bean public BusinessValidationListener businessValidationListener(ValidatingTreeService service) {
            return new BusinessValidationListener(service);
        }
    }
}
