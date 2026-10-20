package org.hswebframework.web.crud.service;

import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.crud.entity.TestTreeSortEntity;
import org.hswebframework.web.crud.events.EntityBeforeCreateEvent;
import org.hswebframework.web.crud.events.EntityBeforeModifyEvent;
import org.hswebframework.web.crud.events.EntityBeforeSaveEvent;
import org.hswebframework.web.crud.events.EntityCreatedEvent;
import org.hswebframework.web.crud.events.EntityModifyEvent;
import org.hswebframework.web.crud.events.EntitySavedEvent;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/** JDBC 与 R2DBC 共用业务森林；期望只来自预先声明的父关系，不复用生产树算法。 */
final class TreeCrudTestSupport {
    private TreeCrudTestSupport() {
    }

    static TestTreeSortEntity node(String group, String parent, String name, Long sort) {
        TestTreeSortEntity node = new TestTreeSortEntity();
        node.setId(IDGenerator.RANDOM.generate());
        node.setGroupId(group);
        node.setParentId(parent);
        node.setName(name);
        node.setDefaultTest("business-" + name);
        node.setSortIndex(sort);
        return node;
    }

    static QueryParamEntity query(String group) {
        return QueryParamEntity.of("groupId", group).noPaging();
    }

    static Set<String> ids(Collection<TestTreeSortEntity> nodes) {
        return nodes.stream().map(TestTreeSortEntity::getId).collect(Collectors.toSet());
    }

    static Map<String, List<Object>> snapshot(Collection<TestTreeSortEntity> nodes) {
        Map<String, List<Object>> snapshot = new LinkedHashMap<>();
        for (TestTreeSortEntity node : nodes) {
            snapshot.put(node.getId(), Arrays.asList(node.getParentId(), node.getPath(), node.getLevel(),
                node.getName(), node.getDefaultTest(), node.getSortIndex(), node.getGroupId()));
        }
        return snapshot;
    }

    static Map<String, List<Object>> businessSnapshot(Collection<TestTreeSortEntity> nodes) {
        Map<String, List<Object>> snapshot = new LinkedHashMap<>();
        for (TestTreeSortEntity node : nodes) {
            snapshot.put(node.getId(), Arrays.asList(node.getName(), node.getDefaultTest(),
                node.getSortIndex(), node.getGroupId()));
        }
        return snapshot;
    }

    static Set<String> subtree(Map<String, String> parents, String root) {
        Set<String> result = new LinkedHashSet<>();
        result.add(root);
        boolean changed;
        do {
            changed = false;
            for (Map.Entry<String, String> node : parents.entrySet()) {
                if (result.contains(node.getValue())) {
                    changed |= result.add(node.getKey());
                }
            }
        } while (changed);
        return result;
    }

    static Map<String, String> subset(Map<String, String> parents, Collection<String> ids) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String id : ids) {
            result.put(id, parents.get(id));
        }
        return result;
    }

    static void assertPersistedTree(Collection<TestTreeSortEntity> nodes, Map<String, String> parents) {
        Set<String> roots = parents.entrySet().stream().filter(entry -> entry.getValue() == null)
            .map(Map.Entry::getKey).collect(Collectors.toSet());
        assertPersistedTree(nodes, parents, roots);
    }

    /** 根节点由场景明确声明，保留原始 parentId 断言，不借用生产根标记判定。 */
    static void assertPersistedTree(Collection<TestTreeSortEntity> nodes, Map<String, String> parents,
                                    Set<String> roots) {
        assertEquals(parents.keySet(), ids(nodes));
        Map<String, TestTreeSortEntity> actual = nodes.stream()
            .collect(Collectors.toMap(TestTreeSortEntity::getId, node -> node));
        for (TestTreeSortEntity node : nodes) {
            assertEquals(parents.get(node.getId()), node.getParentId());
            int level = 1;
            Set<String> ancestors = new HashSet<>();
            for (String ancestor = node.getId(); !roots.contains(ancestor); ) {
                String parent = parents.get(ancestor);
                assertTrue("expected parent is outside the fixture", parents.containsKey(parent));
                assertTrue("expected graph contains a cycle", ancestors.add(parent));
                level++;
                ancestor = parent;
            }
            assertEquals(Integer.valueOf(level), node.getLevel());
            assertNotNull(node.getPath());
            assertEquals(4 + (level - 1) * 5, node.getPath().length());
            if (!roots.contains(node.getId())) {
                TestTreeSortEntity parent = actual.get(node.getParentId());
                assertTrue(node.getPath().startsWith(parent.getPath() + "-"));
            }
        }
    }

    static void assertShape(List<TestTreeSortEntity> roots, Map<String, String> parents, Set<String> rootIds) {
        assertEquals(rootIds, ids(roots));
        Set<String> visited = new HashSet<>();
        Queue<TestTreeSortEntity> pending = new ArrayDeque<>(roots);
        for (TestTreeSortEntity node = pending.poll(); node != null; node = pending.poll()) {
            assertTrue("node repeated in the returned tree", visited.add(node.getId()));
            assertTrue("unexpected node in returned tree", parents.containsKey(node.getId()));
            assertEquals(parents.get(node.getId()), node.getParentId());
            String nodeId = node.getId();
            Set<String> expectedChildren = parents.entrySet().stream()
                .filter(entry -> nodeId.equals(entry.getValue()))
                .map(Map.Entry::getKey).collect(Collectors.toSet());
            List<TestTreeSortEntity> children = node.getChildren();
            assertEquals(expectedChildren, children == null ? Set.of() : ids(children));
            if (children != null) {
                pending.addAll(children);
            }
        }
        assertEquals(parents.keySet(), visited);
    }

    static TestTreeSortEntity returnedNode(List<TestTreeSortEntity> roots, String id) {
        Queue<TestTreeSortEntity> pending = new ArrayDeque<>(roots);
        for (TestTreeSortEntity node = pending.poll(); node != null; node = pending.poll()) {
            if (id.equals(node.getId())) {
                return node;
            }
            if (node.getChildren() != null) {
                pending.addAll(node.getChildren());
            }
        }
        throw new AssertionError("missing returned tree node " + id);
    }

    /** 同步与响应式真实实体事件的执行级快照，操作完成后由测试读取。 */
    static class EntityEvents {
        private final Map<String, List<Object>> beforeCreate = new ConcurrentHashMap<>();
        private final Map<String, List<Object>> created = new ConcurrentHashMap<>();
        private final Map<String, List<Object>> beforeSave = new ConcurrentHashMap<>();
        private final Map<String, List<Object>> saved = new ConcurrentHashMap<>();
        private final Map<String, List<Object>> beforeModify = new ConcurrentHashMap<>();
        private final Map<String, List<Object>> modified = new ConcurrentHashMap<>();

        public void reset() {
            beforeCreate.clear();
            created.clear();
            beforeSave.clear();
            saved.clear();
            beforeModify.clear();
            modified.clear();
        }

        public List<Object> getBeforeCreate(String id) { return beforeCreate.get(id); }
        public List<Object> getCreated(String id) { return created.get(id); }
        public List<Object> getBeforeSave(String id) { return beforeSave.get(id); }
        public List<Object> getSaved(String id) { return saved.get(id); }
        public List<Object> getBeforeModify(String id) { return beforeModify.get(id); }
        public List<Object> getModified(String id) { return modified.get(id); }

        @EventListener
        public void beforeCreate(EntityBeforeCreateEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class) {
                beforeCreate.putAll(TreeCrudTestSupport.snapshot(event.getEntity()));
            }
        }

        @EventListener
        public void created(EntityCreatedEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class) {
                created.putAll(TreeCrudTestSupport.snapshot(event.getEntity()));
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
    }

    /** 在正式 Save 事件中通过真实仓储读取同一事务，分别观察旧库状态与最终输入。 */
    static class SaveEvents extends EntityEvents {
        private final Function<String, Mono<TestTreeSortEntity>> findById;
        private String targetId;
        private Set<String> watchedIds = Set.of();
        private String rejectedId;
        private String callerId;
        List<Object> beforeDatabase;
        List<Object> beforeInput;
        List<Object> callerDatabase;
        final Map<String, List<Object>> savedDatabase = new ConcurrentHashMap<>();
        final List<List<String>> savedBatches = new CopyOnWriteArrayList<>();

        SaveEvents(Function<String, Mono<TestTreeSortEntity>> findById) {
            this.findById = findById;
        }

        void watch(String targetId, Set<String> watchedIds, String rejectedId, String callerId) {
            reset();
            this.targetId = targetId;
            this.watchedIds = watchedIds;
            this.rejectedId = rejectedId;
            this.callerId = callerId;
        }

        @Override
        public void reset() {
            super.reset();
            targetId = null;
            watchedIds = Set.of();
            rejectedId = null;
            callerId = null;
            beforeDatabase = null;
            beforeInput = null;
            callerDatabase = null;
            savedDatabase.clear();
            savedBatches.clear();
        }

        @EventListener
        public void inspectBeforeSave(EntityBeforeSaveEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class && targetId != null
                && event.getEntity().stream().anyMatch(node -> targetId.equals(node.getId()))) {
                beforeInput = snapshot(event.getEntity()).get(targetId);
                event.async(findById.apply(targetId).doOnNext(stored ->
                    beforeDatabase = snapshot(List.of(stored)).get(targetId)).then());
            }
        }

        @EventListener
        public void inspectSaved(EntitySavedEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() != TestTreeSortEntity.class) {
                return;
            }
            List<String> ids = event.getEntity().stream().map(TestTreeSortEntity::getId)
                .filter(watchedIds::contains).collect(Collectors.toList());
            if (ids.isEmpty()) {
                return;
            }
            savedBatches.add(ids);
            Mono<Void> inspection = Flux.fromIterable(ids).concatMap(findById)
                .doOnNext(stored -> savedDatabase.putAll(snapshot(List.of(stored)))).then();
            if (ids.contains(rejectedId)) {
                if (callerId != null) {
                    inspection = inspection.then(findById.apply(callerId).doOnNext(stored ->
                        callerDatabase = snapshot(List.of(stored)).get(callerId)).then());
                }
                inspection = inspection.then(Mono.error(new IllegalStateException("reject saved root marker")));
            }
            event.async(inspection);
        }
    }

    static final class Forest {
        final String group = IDGenerator.RANDOM.generate();
        final Map<String, TestTreeSortEntity> named = new LinkedHashMap<>();
        final Map<String, String> parents = new LinkedHashMap<>();
        final List<TestTreeSortEntity> nodes = new ArrayList<>();

        Forest() {
            add("east", null, 10L);
            add("west", null, 20L);
            add("east-left", "east", 101L);
            add("east-right", "east", 102L);
            add("middle", "east-left", 103L);
            add("deep", "middle", 104L);
            add("deep-leaf", "deep", 105L);
            add("right-leaf", "east-right", 106L);
            add("west-branch", "west", 201L);
            add("west-leaf", "west-branch", 202L);
        }

        private void add(String name, String parentName, long sort) {
            String parentId = parentName == null ? null : id(parentName);
            TestTreeSortEntity node = node(group, parentId, name, sort);
            named.put(name, node);
            nodes.add(node);
            parents.put(node.getId(), parentId);
        }

        String id(String name) {
            return named.get(name).getId();
        }

        Set<String> rootIds() {
            return Set.of(id("east"), id("west"));
        }

        List<TestTreeSortEntity> childFirst() {
            return List.of(named.get("deep-leaf"), named.get("west-leaf"), named.get("middle"),
                named.get("right-leaf"), named.get("west-branch"), named.get("east-right"),
                named.get("east-left"), named.get("deep"), named.get("west"), named.get("east"));
        }
    }
}
