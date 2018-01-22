package org.hswebframework.web.service;

import org.hswebframework.web.commons.entity.PagerResult;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
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

        when(dao.query(queryParamEntity)).then((Answer<List<TestEntity>>) invocationOnMock -> new ArrayList<>(Arrays.asList(TestEntity.builder()
                .age((byte) 10)
                .enabled(true)
                .name("test")
                .build())));

        when(dao.count(queryParamEntity)).thenReturn(1);

        doAnswer(invocationOnMock -> {
            Assert.assertNotEquals(invocationOnMock.getArguments().length, 1);
            Assert.assertNotNull(invocationOnMock.getArguments()[0]);
            return null;
        }).when(dao).insert(anyObject());
    }

    @Test
    public void testQuery() {
        PagerResult<TestEntity> result = entityService.selectPager(queryParamEntity);

        Assert.assertEquals(result.getTotal(), 1);
        Assert.assertEquals(result.getData().size(), 1);

        TestEntity testEntity = TestEntity.builder()
                .age((byte) 1)
                .enabled(true)
                .name("测试")
                .build();

        entityService.insert(testEntity);

        Assert.assertNotNull(testEntity.getId());

        System.out.println(result.getTotal());
    }
}