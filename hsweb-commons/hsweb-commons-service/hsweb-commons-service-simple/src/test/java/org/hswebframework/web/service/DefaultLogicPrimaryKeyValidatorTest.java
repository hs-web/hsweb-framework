package org.hswebframework.web.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.validator.LogicPrimaryKey;
import org.hswebframework.web.validator.group.CreateGroup;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public class DefaultLogicPrimaryKeyValidatorTest {
    DefaultLogicPrimaryKeyValidator validator = new DefaultLogicPrimaryKeyValidator();

    @Test
    public void testSimple() {
        DefaultLogicPrimaryKeyValidator.registerQuerySuppiler(TestBean.class, bean ->
                Query.<TestBean, QueryParamEntity>empty(QueryParamEntity.empty())
                        .setSingleExecutor(param -> {
                            Assert.assertNotNull(param.getTerms());
                            Assert.assertEquals(param.getTerms().size(), 2);
                            return new TestBean("test", "1");
                        }));

        TestBean bean = new TestBean("test", "1");
        Assert.assertTrue(validator.validate(bean).isError());
    }


    @Test
    public void testClassAnn() {
        DefaultLogicPrimaryKeyValidator.registerQuerySuppiler(ClassAnnTestBean.class, bean ->
                Query.<ClassAnnTestBean, QueryParamEntity>empty(QueryParamEntity.empty())
                        .setSingleExecutor(param -> {
                            Assert.assertNotNull(param.getTerms());
                            Assert.assertEquals(param.getTerms().size(), 2);
                            return new ClassAnnTestBean("test", "1");
                        }));

        ClassAnnTestBean bean = new ClassAnnTestBean("test", "1");
        Assert.assertTrue(validator.validate(bean).isError());
    }


    @Test
    public void testGroupAnn() {
        DefaultLogicPrimaryKeyValidator.registerQuerySuppiler(GroupAnnTestBean.class, bean ->
                Query.<GroupAnnTestBean, QueryParamEntity>empty(QueryParamEntity.empty())
                        .setSingleExecutor(param -> {
                            Assert.assertNotNull(param.getTerms());
                            Assert.assertEquals(param.getTerms().size(), 2);
                            return new GroupAnnTestBean("test", "1");
                        }));

        GroupAnnTestBean bean = new GroupAnnTestBean("test", "1");
        Assert.assertTrue(validator.validate(bean).isPassed());

        Assert.assertTrue(validator.validate(bean, TestGroup.class).isError());
    }


    @Test
    public void testNestProperty() {
        NestTestBean nestTestBean=new NestTestBean(new TestBean("test","1"),"test");

        DefaultLogicPrimaryKeyValidator.registerQuerySuppiler(NestTestBean.class, bean ->
                Query.<NestTestBean, QueryParamEntity>empty(QueryParamEntity.empty())
                        .setSingleExecutor(param -> {
                            Assert.assertNotNull(param.getTerms());
                            Assert.assertEquals(param.getTerms().size(), 2);
                            return nestTestBean;
                        }));

        Assert.assertTrue(validator.validate(nestTestBean).isError());
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @LogicPrimaryKey(groups = TestGroup.class)
    public class GroupAnnTestBean {

        private String name;

        private String org;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @LogicPrimaryKey({"name", "org"})
    public class ClassAnnTestBean {

        private String name;

        private String org;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    public class GroupTestBean {

        @LogicPrimaryKey(groups = CreateGroup.class)
        private String name;

        @LogicPrimaryKey
        private String org;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    public class NestTestBean {
        @LogicPrimaryKey("nest.name")
        private TestBean nest;

        @LogicPrimaryKey
        private String org;
    }
    @Getter
    @Setter
    @AllArgsConstructor
    public class TestBean {
        @LogicPrimaryKey
        private String name;

        @LogicPrimaryKey
        private String org;
    }

    @LogicPrimaryKey(value = {"name", "org"}, groups = TestGroup.class)
    public interface TestGroup {

    }
}