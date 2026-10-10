package org.hswebframework.web.crud.service;

import org.hswebframework.web.crud.entity.TestTreeSortEntity;
import org.hswebframework.web.id.IDGenerator;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 同步入口共享path算法的回归；持久图使用替身，不作为同步JDBC CRUD集成证据。 */
public class SyncTreeSortServiceHelperTest {

    @Test
    public void sameBatchParentAndChildMoveKeepsOmittedGrandchildPathConsistent() {
        for (boolean childFirst : List.of(false, true)) {
            for (boolean moveToRoot : List.of(false, true)) {
                TestTreeSortEntity root = node("root", null, "root");
                TestTreeSortEntity target = node("target", null, "next");
                TestTreeSortEntity branch = node("branch", root.getId(), "root-bran");
                TestTreeSortEntity child = node("child", branch.getId(), "root-bran-chld");
                TestTreeSortEntity grandchild = node("grandchild", child.getId(), "root-bran-chld-leaf");
                grandchild.setSortIndex(31L);
                Map<String, TestTreeSortEntity> persisted = List.of(root, target, branch, child, grandchild)
                    .stream().collect(Collectors.toMap(TestTreeSortEntity::getId, value -> value));
                TreeSortEntityService<TestTreeSortEntity, String> service = mock(TreeSortEntityService.class);
                when(service.getIDGenerator()).thenReturn(IDGenerator.MD5);
                when(service.isRootNode(any())).thenCallRealMethod();
                doAnswer(invocation -> {
                    TestTreeSortEntity parent = invocation.getArgument(0);
                    parent.setChildren(invocation.getArgument(1));
                    return null;
                }).when(service).setChildren(any(), anyList());
                when(service.findById(anyCollection())).thenAnswer(invocation -> {
                    Collection<String> ids = invocation.getArgument(0);
                    return ids.stream().filter(persisted::containsKey).map(persisted::get)
                        .map(SyncTreeSortServiceHelperTest::copy).collect(Collectors.toList());
                });
                when(service.queryIncludeChildren(anyCollection())).thenAnswer(invocation -> {
                    Collection<String> ids = invocation.getArgument(0);
                    return persisted.values().stream().filter(candidate -> ids.stream()
                        .map(persisted::get).filter(seed -> seed != null)
                        .anyMatch(seed -> candidate.getId().equals(seed.getId())
                            || candidate.getPath().startsWith(seed.getPath() + "-")))
                        .map(SyncTreeSortServiceHelperTest::copy).collect(Collectors.toList());
                });
                TestTreeSortEntity moved = copy(branch);
                TestTreeSortEntity suppliedChild = copy(child);
                moved.setParentId(moveToRoot ? null : target.getId());
                List<TestTreeSortEntity> prepared = new SyncTreeSortServiceHelper<>(service)
                    .prepare(childFirst ? List.of(suppliedChild, moved) : List.of(moved, suppliedChild));
                Map<String, TestTreeSortEntity> actual = prepared.stream()
                    .collect(Collectors.toMap(TestTreeSortEntity::getId, value -> value));
                assertEquals(3, actual.size());
                TestTreeSortEntity actualBranch = actual.get(branch.getId());
                TestTreeSortEntity actualChild = actual.get(child.getId());
                TestTreeSortEntity actualGrandchild = actual.get(grandchild.getId());
                assertEquals(moveToRoot ? null : target.getId(), actualBranch.getParentId());
                assertEquals(Integer.valueOf(moveToRoot ? 1 : 2), actualBranch.getLevel());
                assertTrue(actualChild.getPath().startsWith(actualBranch.getPath() + "-"));
                assertTrue(actualGrandchild.getPath().startsWith(actualChild.getPath() + "-"));
                assertEquals(Integer.valueOf(actualChild.getLevel() + 1), actualGrandchild.getLevel());
                assertEquals(Long.valueOf(31), actualGrandchild.getSortIndex());
                // 旧快照必须保留；只用它判定持久父级是否变化。
                assertEquals("root-bran-chld-leaf", persisted.get(grandchild.getId()).getPath());
            }
        }
    }

    private static TestTreeSortEntity copy(TestTreeSortEntity source) {
        TestTreeSortEntity copy = node(source.getId(), source.getParentId(), source.getPath());
        copy.setSortIndex(source.getSortIndex());
        return copy;
    }

    private static TestTreeSortEntity node(String id, String parentId, String path) {
        TestTreeSortEntity node = new TestTreeSortEntity();
        node.setId(id);
        node.setName(id);
        node.setParentId(parentId);
        node.setPath(path);
        node.setLevel(path.split("-").length);
        return node;
    }
}
