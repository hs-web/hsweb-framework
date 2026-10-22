package org.hswebframework.web.crud.service;

import org.hswebframework.web.crud.TestApplication;
import org.hswebframework.web.crud.entity.TestTreeSortEntity;
import org.hswebframework.web.id.IDGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/** 普通新增必须分配自己的树路径，不能借用兄弟路径扩大子树查询或删除。 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApplication.class, TestTreeSortEntityService.class}, properties = {
    "spring.r2dbc.generate-unique-name=true",
    "spring.r2dbc.name=NewTreeNodePathRegressionTest"
})
public class NewTreeNodePathRegressionTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Autowired
    private TestTreeSortEntityService service;

    @Test
    public void entitySaveReplacesCopiedSiblingPathBeforeQueryAndDelete() {
        verifyIndependentPath(service::save);
    }

    @Test
    public void publisherSaveReplacesCopiedSiblingPathBeforeQueryAndDelete() {
        verifyIndependentPath(node -> service.save(Mono.just(node)));
    }

    @Test
    public void collectionSaveReplacesCopiedSiblingPathBeforeQueryAndDelete() {
        verifyIndependentPath(node -> service.save(List.of(node)));
    }

    @Test
    public void entityInsertReplacesCopiedSiblingPathBeforeQueryAndDelete() {
        verifyIndependentPath(service::insert);
    }

    @Test
    public void publisherInsertReplacesCopiedSiblingPathBeforeQueryAndDelete() {
        verifyIndependentPath(node -> service.insert(Mono.just(node)));
    }

    @Test
    public void batchInsertReplacesCopiedSiblingPathBeforeQueryAndDelete() {
        verifyIndependentPath(node -> service.insertBatch(Mono.just(List.of(node))));
    }

    private void verifyIndependentPath(Function<TestTreeSortEntity, Mono<?>> writer) {
        TestTreeSortEntity root = node("root");
        TestTreeSortEntity sibling = node("existing-sibling");
        TestTreeSortEntity leaf = node("existing-leaf");
        sibling.setSortIndex(37L);
        leaf.setSortIndex(84L);
        root.setChildren(List.of(sibling));
        sibling.setChildren(List.of(leaf));
        service.save(root).as(StepVerifier::create).expectNextCount(1).verifyComplete();

        TestTreeSortEntity added = node("new-sibling");
        added.setParentId(root.getId());
        added.setPath(sibling.getPath());
        added.setLevel(sibling.getLevel());
        writer.apply(added)
            .then(service.getRepository().findById(added.getId()))
            .flatMap(persisted -> service.queryIncludeChildren(List.of(added.getId())).collectList()
                .flatMap(included -> service.deleteById(added.getId())
                    .flatMap(deleted -> service.getRepository()
                        .findById(List.of(root.getId(), sibling.getId(), leaf.getId(), added.getId()))
                        .collectMap(TestTreeSortEntity::getId)
                        .doOnNext(after -> {
                            Set<String> includedIds = included.stream().map(TestTreeSortEntity::getId)
                                .collect(Collectors.toSet());
                            // 先执行真实子树删除，再一起检查查询结果和数据库，不能靠内存路径断言代替 SQL 边界。
                            assertEquals("new-node query must exclude siblings; deleted=" + deleted
                                + ", remaining=" + after.keySet(), Set.of(added.getId()), includedIds);
                            assertEquals(Integer.valueOf(1), deleted);
                            assertNotEquals(sibling.getPath(), persisted.getPath());
                            assertTrue(persisted.getPath().startsWith(root.getPath() + "-"));
                            assertEquals(Integer.valueOf(2), persisted.getLevel());
                            assertEquals(Set.of(root.getId(), sibling.getId(), leaf.getId()), after.keySet());
                            assertEquals(root.getPath(), after.get(root.getId()).getPath());
                            assertEquals(sibling.getPath(), after.get(sibling.getId()).getPath());
                            assertEquals(leaf.getPath(), after.get(leaf.getId()).getPath());
                            assertEquals("existing-sibling", after.get(sibling.getId()).getName());
                            assertEquals("existing-leaf", after.get(leaf.getId()).getName());
                            assertEquals("business-before", after.get(sibling.getId()).getDefaultTest());
                            assertEquals("business-before", after.get(leaf.getId()).getDefaultTest());
                            assertEquals(Long.valueOf(37), after.get(sibling.getId()).getSortIndex());
                            assertEquals(Long.valueOf(84), after.get(leaf.getId()).getSortIndex());
                        }))))
            .as(StepVerifier::create).expectNextCount(1).expectComplete().verify(TIMEOUT);
    }

    private static TestTreeSortEntity node(String name) {
        TestTreeSortEntity node = new TestTreeSortEntity();
        node.setId(IDGenerator.MD5.generate());
        node.setName(name);
        node.setDefaultTest("business-before");
        return node;
    }
}
