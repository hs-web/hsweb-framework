package org.hswebframework.web.service;

import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.factory.MapperEntityFactory;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.validate.ValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.validation.Validation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author zhouhao
 * @since 3.0
 */
@RunWith(MockitoJUnitRunner.class)
public class GenericEntityServiceTest {

    @InjectMocks
    private TestEntityService entityService = new TestEntityService();

    @Mock
    private CrudDao<TestEntity, String> dao;

    private QueryParamEntity queryParamEntity = new QueryParamEntity();

    @Before
    public void init() {
        entityService.setEntityFactory(new MapperEntityFactory());
        entityService.setValidator(Validation.buildDefaultValidatorFactory().getValidator());

        TestEntity entity = TestEntity.builder()
                .age((byte) 10)
                .enabled(true)
                .name("test")
                .build();
        entity.setId("testId");

        when(dao.query(any()))
                .then((Answer<List<TestEntity>>) invocationOnMock -> new ArrayList<>(Arrays.asList(entity)));

        when(dao.count(any())).thenReturn(1);

        when(dao.update(any())).thenReturn(1);

        when(dao.delete(any())).thenReturn(1);
        when(dao.deleteByPk("test")).thenReturn(1);

        doNothing().when(dao).insert(anyObject());

    }

    @Test
    public void testSimple() {
        Assert.assertEquals(entityService.getEntityType(), TestEntity.class);

        Assert.assertEquals(entityService.getEntityInstanceType(), TestEntity.class);

        Assert.assertEquals(entityService.getPrimaryKeyType(), String.class);
    }

    @Test
    public void testQuery() {
        PagerResult<TestEntity> result = entityService.selectPager(queryParamEntity);
        Assert.assertEquals(result.getTotal(), 1);
        Assert.assertEquals(result.getData().size(), 1);

        TestEntity entity = entityService.selectByPk(result.getData().get(0).getId());
        Assert.assertNotNull(entity);

        List<TestEntity> testEntities = entityService.selectByPk(Arrays.asList(result.getData().get(0).getId()));
        Assert.assertTrue(!testEntities.isEmpty());
    }


    @Test
    public void testInsert() {
        TestEntity testEntity = TestEntity.builder()
                .age((byte) 1)
                .enabled(true)
//                .name("测试")
                .build();
        try {
            entityService.insert(testEntity);
            Assert.assertFalse(true);
        } catch (ValidationException e) {
            Assert.assertFalse(e.getResults().isEmpty());
            Assert.assertEquals(e.getResults().get(0).getField(), "name");
            testEntity.setId(null);
        }
        testEntity.setName("测试");
        String id = entityService.insert(testEntity);
        Assert.assertNotNull(id);
    }

    @Test
    public void testUpdate() {
        TestEntity testEntity = TestEntity.builder()
                .age((byte) 1)
                .enabled(true)
                .name("测试")
                .build();
        testEntity.setId("testEntity");

        int i = entityService.updateByPk("testEntity", testEntity);
        entityService.updateByPk(testEntity);
        entityService.updateByPk(Arrays.asList(testEntity));
        String id = entityService.saveOrUpdate(testEntity);
        Assert.assertEquals(id, testEntity.getId());
        Assert.assertEquals(i, 1);
    }

}