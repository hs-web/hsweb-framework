package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.web.crud.annotation.EnableEasyormRepository;
import org.hswebframework.web.crud.configuration.EasyormConfiguration;
import org.hswebframework.web.crud.configuration.EntityFactoryConfiguration;
import org.hswebframework.web.crud.configuration.JdbcSqlExecutorConfiguration;
import org.hswebframework.web.crud.entity.TestTreeSortEntity;
import org.hswebframework.web.crud.events.EntitySavedEvent;
import org.hswebframework.web.crud.sql.DefaultJdbcExecutor;
import org.hswebframework.web.id.IDGenerator;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.Assert.*;

/** 同步树入口使用真实 JDBC 仓储，验证移根的父 ID、路径和原事务保持一致。 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = SyncTreeCrudRegressionTest.JdbcConfiguration.class)
@TestPropertySource(properties = {"easyorm.dialect=h2", "easyorm.default-schema=PUBLIC"})
public class SyncTreeCrudRegressionTest {

    @Autowired
    private JdbcTreeService service;

    @Autowired
    private SyncSqlExecutor executor;

    @Autowired
    private SavedEvents events;

    @Autowired
    @Qualifier("transactionManager")
    private PlatformTransactionManager transactionManager;

    @After
    public void resetFailure() {
        events.rejectedId = null;
    }

    @Test
    public void updateByIdNamePatchKeepsParentAndPersistedSubtree() {
        assertTrue("the synchronous repository must execute JDBC SQL", executor instanceof DefaultJdbcExecutor);
        List<TestTreeSortEntity> tree = insertTree();
        TestTreeSortEntity branch = stored(tree.get(1).getId());
        TestTreeSortEntity leaf = stored(tree.get(2).getId());
        TestTreeSortEntity patch = new TestTreeSortEntity();
        patch.setName("renamed branch");
        patch.setDefaultTest(branch.getDefaultTest());

        // 此入口继承 save(E) 的普通仓储保存，不把它误当成 save(List) 的树准备路径。
        assertEquals(1, service.updateById(branch.getId(), patch));

        TestTreeSortEntity after = stored(branch.getId());
        assertEquals("renamed branch", after.getName());
        assertSameTreeState(branch, after);
        assertSameTreeState(leaf, stored(leaf.getId()));
        assertEquals(3, service.queryIncludeChildren(List.of(tree.get(0).getId())).size());
    }

    @Test
    public void listSaveMovesBranchAndOmittedLeafToRootInDatabase() {
        List<TestTreeSortEntity> tree = insertTree();
        TestTreeSortEntity root = stored(tree.get(0).getId());
        TestTreeSortEntity branch = stored(tree.get(1).getId());
        TestTreeSortEntity leaf = stored(tree.get(2).getId());
        assertNull(branch.getChildren());
        branch.setParentId(null);

        assertEquals(2, service.save(List.of(branch)).getTotal());

        TestTreeSortEntity moved = stored(branch.getId());
        TestTreeSortEntity movedLeaf = stored(leaf.getId());
        assertNull("a root path must be committed with a cleared parent ID", moved.getParentId());
        assertEquals(Integer.valueOf(1), moved.getLevel());
        assertFalse(moved.getPath().contains("-"));
        assertEquals(moved.getId(), movedLeaf.getParentId());
        assertTrue(movedLeaf.getPath().startsWith(moved.getPath() + "-"));
        assertEquals(Integer.valueOf(2), movedLeaf.getLevel());
        assertEquals(branch.getName(), moved.getName());
        assertEquals(leaf.getName(), movedLeaf.getName());
        assertEquals(branch.getSortIndex(), moved.getSortIndex());
        assertEquals(leaf.getSortIndex(), movedLeaf.getSortIndex());
        assertSameTreeState(root, stored(root.getId()));
        assertEquals(1, service.queryIncludeChildren(List.of(root.getId())).size());
        assertEquals(2, service.queryIncludeChildren(List.of(branch.getId())).size());
    }

    @Test
    public void listRootMoveRollsBackWithSavedEventOrCallerFailure() {
        for (String failure : List.of("event", "caller")) {
            List<TestTreeSortEntity> tree = insertTree();
            TestTreeSortEntity root = stored(tree.get(0).getId());
            TestTreeSortEntity branch = stored(tree.get(1).getId());
            TestTreeSortEntity leaf = stored(tree.get(2).getId());
            TestTreeSortEntity moved = stored(branch.getId());
            moved.setParentId(null);
            if ("event".equals(failure)) {
                events.rejectedId = branch.getId();
            }

            IllegalStateException rejected = assertThrows(IllegalStateException.class, () -> {
                if ("event".equals(failure)) {
                    service.save(List.of(moved));
                } else {
                    new TransactionTemplate(transactionManager).execute(status -> {
                        service.save(List.of(moved));
                        throw new IllegalStateException("reject caller transaction");
                    });
                }
            });
            assertTrue(rejected.getMessage().contains("reject"));
            events.rejectedId = null;

            assertSameTreeState(root, stored(root.getId()));
            assertSameTreeState(branch, stored(branch.getId()));
            assertSameTreeState(leaf, stored(leaf.getId()));
            assertEquals(3, service.queryIncludeChildren(List.of(root.getId())).size());
        }
    }

    private List<TestTreeSortEntity> insertTree() {
        TestTreeSortEntity root = node(null, "root");
        TestTreeSortEntity branch = node(root.getId(), "branch");
        TestTreeSortEntity leaf = node(branch.getId(), "leaf");
        List<TestTreeSortEntity> tree = List.of(root, branch, leaf);
        assertEquals(3, service.insert(tree));
        return tree;
    }

    private TestTreeSortEntity stored(String id) {
        return service.findById(id).orElseThrow(AssertionError::new);
    }

    private static TestTreeSortEntity node(String parentId, String name) {
        TestTreeSortEntity node = new TestTreeSortEntity();
        node.setId(IDGenerator.RANDOM.generate());
        node.setParentId(parentId);
        node.setName(name);
        // 同步事件在 ORM 应用列默认值前校验 CreateGroup，夹具必须提供合法必填值。
        node.setDefaultTest("business-before");
        node.setSortIndex(17L);
        return node;
    }

    private static void assertSameTreeState(TestTreeSortEntity expected, TestTreeSortEntity actual) {
        assertEquals(expected.getParentId(), actual.getParentId());
        assertEquals(expected.getPath(), actual.getPath());
        assertEquals(expected.getLevel(), actual.getLevel());
        assertEquals(expected.getSortIndex(), actual.getSortIndex());
    }

    static class JdbcTreeService extends GenericTreeSupportCrudService<TestTreeSortEntity, String> {
        @Override
        public IDGenerator<String> getIDGenerator() {
            return IDGenerator.MD5;
        }

        @Override
        public void setChildren(TestTreeSortEntity entity, List<TestTreeSortEntity> children) {
            entity.setChildren(children);
        }
    }

    static class SavedEvents {
        private volatile String rejectedId;

        @EventListener
        public void saved(EntitySavedEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class
                && event.getEntity().stream().anyMatch(entity -> entity.getId().equals(rejectedId))) {
                event.async(Mono.error(new IllegalStateException("reject saved tree event")));
            }
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement(proxyTargetClass = true)
    @EnableEasyormRepository(value = "org.hswebframework.web.crud.entity.TestTreeSortEntity",
        reactive = false, nonReactive = true)
    @ImportAutoConfiguration({EasyormConfiguration.class, EntityFactoryConfiguration.class, JdbcSqlExecutorConfiguration.class})
    static class JdbcConfiguration {
        @Bean(destroyMethod = "shutdown")
        EmbeddedDatabase dataSource() {
            return new EmbeddedDatabaseBuilder().generateUniqueName(true).setType(EmbeddedDatabaseType.H2).build();
        }

        @Bean
        JdbcTreeService jdbcTreeService() {
            return new JdbcTreeService();
        }

        @Bean
        SavedEvents savedEvents() {
            return new SavedEvents();
        }
    }
}
