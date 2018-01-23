package org.hswebframework.web.service;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.commons.entity.factory.MapperEntityFactory;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.dao.CrudDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.validation.Validation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * @author zhouhao
 * @since 3.0
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractTreeSortServiceTests {

    @InjectMocks
    private TestTreeEntityService entityService = new TestTreeEntityService();

    @Mock
    private CrudDao<TestEntity, String> dao;

    private List<TestEntity> entities = new ArrayList<>();

    @Before
    public void init() {
        entityService.setEntityFactory(new MapperEntityFactory());

        TestEntity entity = TestEntity.builder()
                .age((byte) 10)
                .enabled(true)
                .name("test")
                .build();
        entity.setId("testId");

        when(dao.query(any()))
                .thenReturn(new ArrayList<>());

        when(dao.count(any())).thenReturn(1);

//        when(dao.update(any())).thenReturn(1);

        when(dao.delete(any())).thenReturn(1);
        when(dao.deleteByPk("test")).thenReturn(1);

        //  doNothing().when(dao).insert(anyObject());
        doAnswer(invocationOnMock -> {
            TestEntity t = invocationOnMock.getArgumentAt(0, TestEntity.class);
            entities.add(t);
            return t.getId();
        }).when(dao).insert(anyObject());

        doAnswer(invocationOnMock -> {
            TestEntity t = invocationOnMock.getArgumentAt(0, TestEntity.class);
            entities.add(t);
            return t.getId();
        }).when(dao).update(anyObject());
    }

    @Test
    public void testBatchInsert() {
        String treeJson = "{'id':'1','parentId':'-1','name':'父节点','children':[" +
                "{'id':'101','parentId':'1','name':'子节点1'}," +
                "{'id':'102','parentId':'1','name':'子节点2'}" +
                "]}";
        TestEntity entity = JSON.parseObject(treeJson, TestEntity.class);

        entityService.insert(entity);

        Assert.assertEquals(entities.size(), 3);
        entities.clear();

        entityService.updateByPk(entity);
        Assert.assertEquals(entities.size(), 3);
        entities.clear();

        entityService.updateBatch(Arrays.asList(entity));
        Assert.assertEquals(entities.size(), 3);
    }
}