package org.hswebframework.web.crud.service;

import org.hswebframework.web.crud.TestApplication;
import org.hswebframework.web.crud.entity.TestTreeSortEntity;
import org.hswebframework.web.crud.events.EntityBeforeModifyEvent;
import org.hswebframework.web.crud.events.EntityBeforeSaveEvent;
import org.hswebframework.web.crud.events.EntityModifyEvent;
import org.hswebframework.web.crud.events.EntitySavedEvent;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceSaveController;
import org.hswebframework.web.exception.ValidationException;
import org.hswebframework.web.id.IDGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/** 显式嵌套节点保留修改和创建语义，自动展开的后代只维护树字段。 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApplication.class, TestTreeSortEntityService.class,
    ExplicitTreeUpdateRegressionTest.HttpEventConfiguration.class}, properties = {
    "spring.r2dbc.generate-unique-name=true",
    "spring.r2dbc.name=ExplicitTreeUpdateRegressionTest"
})
public class ExplicitTreeUpdateRegressionTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Autowired
    private TestTreeSortEntityService service;

    @Autowired
    private HttpTreeEventListener httpEvents;

    @Test
    public void monoPatchUpdatesExplicitExistingChildAndPreservesOmittedFields() {
        verifyExistingChild(false);
    }

    @Test
    public void entityPatchUpdatesExplicitExistingChildAndPreservesOmittedFields() {
        verifyExistingChild(true);
    }

    @Test
    public void monoPatchInsertsExplicitNewChild() {
        verifyNewChild(false);
    }

    @Test
    public void entityPatchInsertsExplicitNewChild() {
        verifyNewChild(true);
    }

    @Test
    public void monoNestedChildMovePreservesOmittedLeafBusinessState() {
        verifyNestedChildMove(false);
    }

    @Test
    public void entityNestedChildMovePreservesOmittedLeafBusinessState() {
        verifyNestedChildMove(true);
    }

    @Test
    public void monoNewChildValidationRollsBackTheWholePatch() {
        verifyInvalidNewChild(false);
    }

    @Test
    public void entityNewChildValidationRollsBackTheWholePatch() {
        verifyInvalidNewChild(true);
    }

    @Test
    public void monoNewParentKeepsExplicitExistingChildAndOmittedLeafPathsConsistent() {
        verifyNewParentWithExistingChild(false);
    }

    @Test
    public void entityNewParentKeepsExplicitExistingChildAndOmittedLeafPathsConsistent() {
        verifyNewParentWithExistingChild(true);
    }

    @Test
    public void monoNamePatchObjectReusePreservesCommittedBusinessAndMove() {
        verifyReusedNamePatch(false, false);
    }

    @Test
    public void entityNamePatchObjectReusePreservesCommittedBusinessAndMove() {
        verifyReusedNamePatch(true, false);
    }

    @Test
    public void monoNamePatchPublisherResubscriptionPreservesCommittedBusinessAndMove() {
        verifyReusedNamePatch(false, true);
    }

    @Test
    public void entityNamePatchPublisherResubscriptionPreservesCommittedBusinessAndMove() {
        verifyReusedNamePatch(true, true);
    }

    @Test
    public void publicPutSparseNameUpdatePreservesParentAndOmittedSubtree() {
        TestTreeSortEntity root = node("root-before");
        TestTreeSortEntity child = node("child-before");
        TestTreeSortEntity leaf = node("leaf-before");
        child.setSortIndex(37L);
        leaf.setSortIndex(84L);
        leaf.setDefaultTest("leaf-business");
        root.setChildren(List.of(child));
        child.setChildren(List.of(leaf));
        save(root);
        List<String> ids = List.of(root.getId(), child.getId(), leaf.getId());
        Map<String, TestTreeSortEntity> before = stored(ids).block(TIMEOUT);
        Map<String, List<Object>> beforeSnapshot = TreeCrudTestSupport.snapshot(before.values());
        for (boolean explicitNull : List.of(false, true)) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("name", "renamed-put-" + explicitNull);
            payload.put("defaultTest", null);
            if (explicitNull) {
                payload.put("parentId", null);
            }
            httpEvents.reset();
            client().put().uri("/tree/{id}", child.getId()).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload).exchange().expectStatus().isOk()
                .expectBody(Boolean.class).isEqualTo(true);
            stored(ids).as(StepVerifier::create).assertNext(after -> {
                TestTreeSortEntity updated = after.get(child.getId());
                assertEquals("renamed-put-" + explicitNull, updated.getName());
                assertEquals("business-before", updated.getDefaultTest());
                assertEquals(root.getId(), updated.getParentId());
                assertEquals(before.get(child.getId()).getPath(), updated.getPath());
                assertEquals(Integer.valueOf(2), updated.getLevel());
                assertEquals(Long.valueOf(37), updated.getSortIndex());
                Map<String, List<Object>> afterSnapshot = TreeCrudTestSupport.snapshot(after.values());
                assertEquals(beforeSnapshot.get(root.getId()), afterSnapshot.get(root.getId()));
                assertEquals(beforeSnapshot.get(leaf.getId()), afterSnapshot.get(leaf.getId()));
                assertEquals(afterSnapshot.get(child.getId()), httpEvents.getBeforeModify(child.getId()));
                assertEquals(afterSnapshot.get(child.getId()), httpEvents.getModified(child.getId()));
                assertNull("existing PUT must use Modify rather than Save", httpEvents.getBeforeSave(child.getId()));
                assertNull(httpEvents.getSaved(child.getId()));
            }).expectComplete().verify(TIMEOUT);
            service.queryIncludeChildren(List.of(root.getId())).count()
                .as(StepVerifier::create).expectNext(3L).verifyComplete();
        }
    }

    @Test
    public void publicPatchFullNullParentMovesSubtreeAndCreatesMissingId() {
        TestTreeSortEntity root = node("root-before");
        TestTreeSortEntity child = node("child-before");
        TestTreeSortEntity leaf = node("leaf-before");
        child.setSortIndex(37L);
        leaf.setSortIndex(84L);
        leaf.setDefaultTest("leaf-business");
        root.setChildren(List.of(child));
        child.setChildren(List.of(leaf));
        save(root);
        Map<String, TestTreeSortEntity> before = stored(List.of(root.getId(), child.getId(), leaf.getId()))
            .block(TIMEOUT);
        Map<String, List<Object>> beforeSnapshot = TreeCrudTestSupport.snapshot(before.values());
        TestTreeSortEntity complete = before.get(child.getId());
        complete.setParentId(null);
        complete.setName("moved-root");
        TestTreeSortEntity added = node("http-created-root");
        service.findById(added.getId()).as(StepVerifier::create).verifyComplete();
        httpEvents.reset();
        // 完整表达提交节点的树关系；遗漏 children 仍保留原后代，不作为删除清单。
        client().patch().uri("/tree").contentType(MediaType.APPLICATION_JSON)
            .bodyValue(List.of(fullSavePayload(complete), fullSavePayload(added)))
            .exchange().expectStatus().isOk();
        stored(List.of(root.getId(), child.getId(), leaf.getId(), added.getId()))
            .as(StepVerifier::create).assertNext(after -> {
                assertEquals(4, after.size());
                TestTreeSortEntity moved = after.get(child.getId());
                TestTreeSortEntity movedLeaf = after.get(leaf.getId());
                TestTreeSortEntity created = after.get(added.getId());
                assertNull(moved.getParentId());
                assertEquals("moved-root", moved.getName());
                assertEquals("business-before", moved.getDefaultTest());
                assertEquals(Long.valueOf(37), moved.getSortIndex());
                assertEquals(Integer.valueOf(1), moved.getLevel());
                assertFalse(moved.getPath().contains("-"));
                assertNotEquals(beforeSnapshot.get(child.getId()).get(1), moved.getPath());
                assertEquals(child.getId(), movedLeaf.getParentId());
                assertTrue(movedLeaf.getPath().startsWith(moved.getPath() + "-"));
                assertEquals(Integer.valueOf(2), movedLeaf.getLevel());
                assertEquals("leaf-before", movedLeaf.getName());
                assertEquals("leaf-business", movedLeaf.getDefaultTest());
                assertEquals(Long.valueOf(84), movedLeaf.getSortIndex());
                assertNull(created.getParentId());
                assertEquals("http-created-root", created.getName());
                assertEquals("business-before", created.getDefaultTest());
                assertEquals(Integer.valueOf(1), created.getLevel());
                assertFalse(created.getPath().contains("-"));
                Map<String, List<Object>> afterSnapshot = TreeCrudTestSupport.snapshot(after.values());
                assertEquals(beforeSnapshot.get(root.getId()), afterSnapshot.get(root.getId()));
                for (String id : List.of(child.getId(), leaf.getId(), added.getId())) {
                    assertEquals(afterSnapshot.get(id), httpEvents.getBeforeSave(id));
                    assertEquals(afterSnapshot.get(id), httpEvents.getSaved(id));
                }
                assertNull(httpEvents.getBeforeSave(root.getId()));
                assertNotNull("root clear retains the formal Modify boundary", httpEvents.getModified(child.getId()));
                assertNull(httpEvents.getBeforeModify(child.getId()).get(0));
                assertNull(httpEvents.getModified(child.getId()).get(0));
                assertEquals(moved.getPath(), httpEvents.getModified(child.getId()).get(1));
                assertEquals(Integer.valueOf(1), httpEvents.getModified(child.getId()).get(2));
            }).expectComplete().verify(TIMEOUT);
        service.queryIncludeChildren(List.of(root.getId())).count()
            .as(StepVerifier::create).expectNext(1L).verifyComplete();
        service.queryIncludeChildren(List.of(child.getId())).count()
            .as(StepVerifier::create).expectNext(2L).verifyComplete();
    }

    private static Map<String, Object> fullSavePayload(TestTreeSortEntity entity) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", entity.getId());
        payload.put("name", entity.getName());
        payload.put("defaultTest", entity.getDefaultTest());
        payload.put("groupId", entity.getGroupId());
        payload.put("parentId", entity.getParentId());
        payload.put("path", entity.getPath());
        payload.put("level", entity.getLevel());
        payload.put("sortIndex", entity.getSortIndex());
        return payload;
    }

    @Test
    public void publicPutPersistsBothExistingAndGeneratedIdChildren() {
        TestTreeSortEntity root = node("root-before");
        TestTreeSortEntity child = node("child-before");
        child.setSortIndex(37L);
        root.setChildren(List.of(child));
        save(root);
        TestTreeSortEntity patch = patch("root-after");
        TestTreeSortEntity childPatch = patch("child-after");
        childPatch.setId(child.getId());
        TestTreeSortEntity added = patch("http-new-" + IDGenerator.MD5.generate());
        added.setDefaultTest("new-business");
        patch.setChildren(List.of(childPatch, added));
        client().put().uri("/tree/{id}", root.getId()).contentType(MediaType.APPLICATION_JSON)
            .bodyValue(patch).exchange().expectStatus().isOk().expectBody(Boolean.class).isEqualTo(true);
        stored(List.of(root.getId(), child.getId())).as(StepVerifier::create).assertNext(after -> {
            assertEquals("root-after", after.get(root.getId()).getName());
            assertEquals("child-after", after.get(child.getId()).getName());
            assertEquals("business-before", after.get(child.getId()).getDefaultTest());
            assertEquals(Long.valueOf(37), after.get(child.getId()).getSortIndex());
        }).expectComplete().verify(TIMEOUT);
        findNewChild(added.getName(), root.getId()).as(StepVerifier::create).assertNext(after -> {
            assertNotNull(after.getId());
            assertEquals("new-business", after.getDefaultTest());
            assertTrue(after.getPath().startsWith(root.getPath() + "-"));
            assertEquals(Integer.valueOf(2), after.getLevel());
        }).expectComplete().verify(TIMEOUT);
    }

    @Test
    public void publicPutRejectsInvalidNewChildAndRollsBackParent() {
        TestTreeSortEntity root = node("root-before");
        save(root);
        TestTreeSortEntity patch = patch("root-after");
        TestTreeSortEntity invalid = patch("http-invalid-" + IDGenerator.MD5.generate());
        invalid.setDefaultTest("");
        patch.setChildren(List.of(invalid));
        // 与生产相同的 CreateGroup 约束先通过正式新增入口验证，HTTP 不替换事件校验。
        service.insert(invalid).as(StepVerifier::create).expectError(ValidationException.class).verify(TIMEOUT);
        client().put().uri("/tree/{id}", root.getId()).contentType(MediaType.APPLICATION_JSON)
            .bodyValue(patch).exchange().expectStatus().value(status -> assertTrue(status >= 400));
        verifyRejectedPatch(root, invalid.getName());
    }

    @Test
    public void missingTargetDoesNotCreateExplicitChildren() {
        for (boolean entityOverload : List.of(false, true)) {
            String missingId = IDGenerator.MD5.generate();
            TestTreeSortEntity patch = patch("missing-target");
            TestTreeSortEntity child = node("missing-child-" + IDGenerator.MD5.generate());
            patch.setChildren(List.of(child));
            update(missingId, patch, entityOverload).as(StepVerifier::create).expectNext(0).verifyComplete();
            service.getRepository().createQuery().where("name", child.getName()).count()
                .as(StepVerifier::create).expectNext(0).verifyComplete();
        }
    }

    private void verifyExistingChild(boolean entityOverload) {
        TestTreeSortEntity outer = node("outer");
        TestTreeSortEntity root = node("root-before");
        TestTreeSortEntity child = node("child-before");
        child.setSortIndex(37L);
        TestTreeSortEntity leaf = node("leaf-before");
        leaf.setSortIndex(84L);
        outer.setChildren(List.of(root));
        root.setChildren(List.of(child));
        child.setChildren(List.of(leaf));
        save(outer);
        TestTreeSortEntity patch = patch("root-after");
        TestTreeSortEntity childPatch = patch("child-after");
        childPatch.setId(child.getId());
        patch.setChildren(List.of(childPatch));
        update(root.getId(), patch, entityOverload).then(stored(List.of(root.getId(), child.getId(), leaf.getId())))
            .as(StepVerifier::create).assertNext(after -> {
                assertEquals("root-after", after.get(root.getId()).getName());
                assertEquals(outer.getId(), after.get(root.getId()).getParentId());
                assertEquals(root.getPath(), after.get(root.getId()).getPath());
                TestTreeSortEntity storedChild = after.get(child.getId());
                assertEquals("child-after", storedChild.getName());
                assertEquals("business-before", storedChild.getDefaultTest());
                assertEquals(root.getId(), storedChild.getParentId());
                assertEquals(child.getPath(), storedChild.getPath());
                assertEquals(child.getSortIndex(), storedChild.getSortIndex());
                assertEquals("leaf-before", after.get(leaf.getId()).getName());
                assertEquals(leaf.getPath(), after.get(leaf.getId()).getPath());
                assertEquals(leaf.getSortIndex(), after.get(leaf.getId()).getSortIndex());
            }).expectComplete().verify(TIMEOUT);
    }

    private void verifyReusedNamePatch(boolean entityOverload, boolean reusePublisher) {
        TestTreeSortEntity oldRoot = node("old-root");
        TestTreeSortEntity targetRoot = node("target-root");
        TestTreeSortEntity targetBranch = node("target-branch");
        TestTreeSortEntity target = node("target");
        TestTreeSortEntity branch = node("branch-before");
        TestTreeSortEntity leaf = node("leaf-before");
        branch.setSortIndex(37L);
        leaf.setSortIndex(84L);
        oldRoot.setChildren(List.of(branch));
        branch.setChildren(List.of(leaf));
        targetRoot.setChildren(List.of(targetBranch));
        targetBranch.setChildren(List.of(target));
        List<String> ids = List.of(oldRoot.getId(), targetRoot.getId(), targetBranch.getId(),
                                   target.getId(), branch.getId(), leaf.getId());
        Map<String, String> movedParents = new LinkedHashMap<>();
        movedParents.put(oldRoot.getId(), null);
        movedParents.put(targetRoot.getId(), null);
        movedParents.put(targetBranch.getId(), targetRoot.getId());
        movedParents.put(target.getId(), targetBranch.getId());
        movedParents.put(branch.getId(), target.getId());
        movedParents.put(leaf.getId(), branch.getId());
        service.save(List.of(oldRoot, targetRoot))
            .as(StepVerifier::create).expectNextCount(1).expectComplete().verify(TIMEOUT);
        try {
            TestTreeSortEntity namePatch = patch("branch-after");
            Mono<Integer> firstUpdate = update(branch.getId(), namePatch, entityOverload);
            firstUpdate.as(StepVerifier::create).expectNext(1).expectComplete().verify(TIMEOUT);
            stored(ids).as(StepVerifier::create).assertNext(after -> {
                assertEquals("branch-after", after.get(branch.getId()).getName());
                assertEquals("business-before", after.get(branch.getId()).getDefaultTest());
                assertEquals(oldRoot.getId(), after.get(branch.getId()).getParentId());
            }).expectComplete().verify(TIMEOUT);

            // 每个订阅完成后再发起下一次操作，业务修改和移动已在独立事务中提交。
            TestTreeSortEntity businessPatch = new TestTreeSortEntity();
            businessPatch.setDefaultTest("business-after");
            update(branch.getId(), businessPatch, entityOverload)
                .as(StepVerifier::create).expectNext(1).expectComplete().verify(TIMEOUT);
            service.findById(branch.getId()).flatMap(existing -> {
                existing.setParentId(target.getId());
                return service.save(existing);
            }).as(StepVerifier::create).expectNextCount(1).expectComplete().verify(TIMEOUT);
            AtomicReference<Map<String, List<Object>>> committed = new AtomicReference<>();
            stored(ids).as(StepVerifier::create).assertNext(moved -> {
                TreeCrudTestSupport.assertPersistedTree(moved.values(), movedParents);
                assertEquals("branch-after", moved.get(branch.getId()).getName());
                assertEquals("business-after", moved.get(branch.getId()).getDefaultTest());
                assertEquals("leaf-before", moved.get(leaf.getId()).getName());
                assertEquals("business-before", moved.get(leaf.getId()).getDefaultTest());
                assertEquals(Long.valueOf(37), moved.get(branch.getId()).getSortIndex());
                assertEquals(Long.valueOf(84), moved.get(leaf.getId()).getSortIndex());
                committed.set(TreeCrudTestSupport.snapshot(moved.values()));
            }).expectComplete().verify(TIMEOUT);

            // 分别复用调用方对象创建新操作、以及重新订阅原操作；两者仍只有名称修改意图。
            Mono<Integer> repeated = reusePublisher ? firstUpdate : update(branch.getId(), namePatch, entityOverload);
            repeated.as(StepVerifier::create).expectNextCount(1).expectComplete().verify(TIMEOUT);
            stored(ids).as(StepVerifier::create).assertNext(after -> {
                assertEquals("business-after", after.get(branch.getId()).getDefaultTest());
                assertEquals(target.getId(), after.get(branch.getId()).getParentId());
                TreeCrudTestSupport.assertPersistedTree(after.values(), movedParents);
                assertEquals(committed.get(), TreeCrudTestSupport.snapshot(after.values()));
            }).expectComplete().verify(TIMEOUT);
        } finally {
            // 清理固定六个输入实体，避免依赖被测子树查询和删除路径。
            service.getRepository().createDelete().in("id", ids).execute()
                .as(StepVerifier::create).expectNext(6).expectComplete().verify(TIMEOUT);
        }
    }

    private void verifyNewChild(boolean entityOverload) {
        TestTreeSortEntity root = node("root-before");
        save(root);
        TestTreeSortEntity patch = patch("root-after");
        TestTreeSortEntity added = patch("new-child-" + IDGenerator.MD5.generate());
        added.setDefaultTest("new-business");
        patch.setChildren(List.of(added));
        update(root.getId(), patch, entityOverload).then(findNewChild(added.getName(), root.getId()))
            .as(StepVerifier::create).assertNext(after -> {
                assertNotNull(after.getId());
                assertEquals("new-business", after.getDefaultTest());
                assertEquals(root.getId(), after.getParentId());
                assertTrue(after.getPath().startsWith(root.getPath() + "-"));
                assertEquals(Integer.valueOf(2), after.getLevel());
            }).expectComplete().verify(TIMEOUT);
    }

    private void verifyNestedChildMove(boolean entityOverload) {
        TestTreeSortEntity parentA = node("parent-a");
        TestTreeSortEntity parentB = node("parent-b");
        TestTreeSortEntity branch = node("branch-before");
        branch.setSortIndex(37L);
        TestTreeSortEntity leaf = node("leaf-before");
        leaf.setSortIndex(84L);
        parentA.setChildren(List.of(branch));
        branch.setChildren(List.of(leaf));
        service.save(List.of(parentA, parentB)).as(StepVerifier::create).expectNextCount(1).verifyComplete();
        TestTreeSortEntity patch = patch("parent-b-after");
        TestTreeSortEntity childPatch = patch("branch-after");
        childPatch.setId(branch.getId());
        patch.setChildren(List.of(childPatch));
        update(parentB.getId(), patch, entityOverload).then(stored(List.of(branch.getId(), leaf.getId())))
            .as(StepVerifier::create).assertNext(after -> {
                TestTreeSortEntity moved = after.get(branch.getId());
                assertEquals("branch-after", moved.getName());
                assertEquals("business-before", moved.getDefaultTest());
                assertEquals(parentB.getId(), moved.getParentId());
                assertTrue(moved.getPath().startsWith(parentB.getPath() + "-"));
                assertEquals(branch.getSortIndex(), moved.getSortIndex());
                TestTreeSortEntity storedLeaf = after.get(leaf.getId());
                assertEquals(branch.getId(), storedLeaf.getParentId());
                assertTrue(storedLeaf.getPath().startsWith(moved.getPath() + "-"));
                assertEquals(Integer.valueOf(3), storedLeaf.getLevel());
                assertEquals("leaf-before", storedLeaf.getName());
                assertEquals("business-before", storedLeaf.getDefaultTest());
                assertEquals(leaf.getSortIndex(), storedLeaf.getSortIndex());
            }).expectComplete().verify(TIMEOUT);
    }

    private void verifyNewParentWithExistingChild(boolean entityOverload) {
        TestTreeSortEntity root = node("root-before");
        TestTreeSortEntity branch = node("branch-before");
        TestTreeSortEntity leaf = node("leaf-before");
        root.setChildren(List.of(branch));
        branch.setChildren(List.of(leaf));
        save(root);
        TestTreeSortEntity patch = patch("root-after");
        TestTreeSortEntity newParent = patch("new-parent-" + IDGenerator.MD5.generate());
        newParent.setDefaultTest("new-business");
        TestTreeSortEntity branchPatch = patch("branch-after");
        branchPatch.setId(branch.getId());
        newParent.setChildren(List.of(branchPatch));
        patch.setChildren(List.of(newParent));
        update(root.getId(), patch, entityOverload).then(findNewChild(newParent.getName(), root.getId()))
            .flatMap(parent -> stored(List.of(branch.getId(), leaf.getId())).map(after -> {
                TestTreeSortEntity storedBranch = after.get(branch.getId());
                TestTreeSortEntity storedLeaf = after.get(leaf.getId());
                assertEquals(parent.getId(), storedBranch.getParentId());
                assertEquals("branch-after", storedBranch.getName());
                assertEquals("business-before", storedBranch.getDefaultTest());
                assertTrue(storedBranch.getPath().startsWith(parent.getPath() + "-"));
                assertEquals(Integer.valueOf(3), storedBranch.getLevel());
                assertEquals(branch.getId(), storedLeaf.getParentId());
                assertEquals("leaf-before", storedLeaf.getName());
                assertTrue(storedLeaf.getPath().startsWith(storedBranch.getPath() + "-"));
                assertEquals(Integer.valueOf(4), storedLeaf.getLevel());
                return true;
            })).as(StepVerifier::create).expectNext(true).expectComplete().verify(TIMEOUT);
    }

    private void verifyInvalidNewChild(boolean entityOverload) {
        TestTreeSortEntity root = node("root-before");
        save(root);
        TestTreeSortEntity patch = patch("root-after");
        TestTreeSortEntity invalid = patch("invalid-" + IDGenerator.MD5.generate());
        invalid.setDefaultTest("");
        patch.setChildren(List.of(invalid));
        service.insert(invalid).as(StepVerifier::create).expectError(ValidationException.class).verify(TIMEOUT);
        update(root.getId(), patch, entityOverload).as(StepVerifier::create)
            .expectError(ValidationException.class).verify(TIMEOUT);
        verifyRejectedPatch(root, invalid.getName());
    }

    private void verifyRejectedPatch(TestTreeSortEntity root, String invalidName) {
        service.getRepository().findById(root.getId()).as(StepVerifier::create).assertNext(after -> {
            assertEquals("root-before", after.getName());
            assertEquals(root.getPath(), after.getPath());
        }).expectComplete().verify(TIMEOUT);
        service.getRepository().createQuery().where("name", invalidName).count()
            .as(StepVerifier::create).expectNext(0).verifyComplete();
    }

    private Mono<Integer> update(String id, TestTreeSortEntity patch, boolean entityOverload) {
        return entityOverload ? service.updateById(id, patch) : service.updateById(id, Mono.just(patch));
    }

    private Mono<Map<String, TestTreeSortEntity>> stored(List<String> ids) {
        return service.getRepository().findById(ids).collectMap(TestTreeSortEntity::getId);
    }

    private Mono<TestTreeSortEntity> findNewChild(String name, String parentId) {
        return service.getRepository().createQuery().where("name", name).and("parentId", parentId).fetchOne();
    }

    private void save(TestTreeSortEntity root) {
        service.save(root).as(StepVerifier::create).expectNextCount(1).verifyComplete();
    }

    private WebTestClient client() {
        return WebTestClient.bindToController(new TreeController(service)).build();
    }

    private static TestTreeSortEntity node(String name) {
        TestTreeSortEntity node = patch(name);
        node.setId(IDGenerator.MD5.generate());
        node.setDefaultTest("business-before");
        return node;
    }

    private static TestTreeSortEntity patch(String name) {
        TestTreeSortEntity patch = new TestTreeSortEntity();
        patch.setName(name);
        return patch;
    }

    /** HTTP 入口经过真实仓储后，核对正式事件提供的完整树与业务字段。 */
    public static class HttpTreeEventListener {
        private final Map<String, List<Object>> beforeModify = new ConcurrentHashMap<>();
        private final Map<String, List<Object>> modified = new ConcurrentHashMap<>();
        private final Map<String, List<Object>> beforeSave = new ConcurrentHashMap<>();
        private final Map<String, List<Object>> saved = new ConcurrentHashMap<>();

        public void reset() {
            beforeModify.clear();
            modified.clear();
            beforeSave.clear();
            saved.clear();
        }

        public List<Object> getBeforeModify(String id) { return beforeModify.get(id); }
        public List<Object> getModified(String id) { return modified.get(id); }
        public List<Object> getBeforeSave(String id) { return beforeSave.get(id); }
        public List<Object> getSaved(String id) { return saved.get(id); }

        @EventListener
        public void beforeModify(EntityBeforeModifyEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class) {
                beforeModify.putAll(TreeCrudTestSupport.snapshot(event.getAfter()));
            }
        }

        @EventListener
        public void modified(EntityModifyEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class) {
                modified.putAll(TreeCrudTestSupport.snapshot(event.getAfter()));
            }
        }

        @EventListener
        public void beforeSave(EntityBeforeSaveEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class) {
                beforeSave.putAll(TreeCrudTestSupport.snapshot(event.getEntity()));
            }
        }

        @EventListener
        public void saved(EntitySavedEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class) {
                saved.putAll(TreeCrudTestSupport.snapshot(event.getEntity()));
            }
        }
    }

    @TestConfiguration
    public static class HttpEventConfiguration {
        @Bean
        public HttpTreeEventListener httpTreeEventListener() {
            return new HttpTreeEventListener();
        }
    }

    @TestComponent
    @RestController
    @RequestMapping("/tree")
    public static class TreeController implements ReactiveServiceSaveController<TestTreeSortEntity, String> {
        private final TestTreeSortEntityService service;

        public TreeController(TestTreeSortEntityService service) {
            this.service = service;
        }

        @Override
        public TestTreeSortEntityService getService() {
            return service;
        }
    }
}
