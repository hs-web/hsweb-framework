package org.hswebframework.web.service;

import org.hswebframework.web.commons.entity.factory.MapperEntityFactory;
import org.hswebframework.web.dao.CrudDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.suppliers.TestedOn;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.Validation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author zhouhao
 * @since 3.0
 */
@SpringBootTest(classes = SpringTestApplication.class)
@RunWith(value = SpringRunner.class)
public class EnableCacheTests {

    @Autowired
    private EnableCacheTestService enableCacheTestService;

    @Autowired
    private EnableCacheAllEvictTestService     enableCacheAllEvictTestService;
    @Autowired
    private EnableCacheTreeTestService         enableCacheTreeTestService;
    @Autowired
    private EnableCacheAllEvictTreeTestService enableCacheAllEvictTreeTestService;

    private AtomicInteger counter = new AtomicInteger();

    @Before
    public void init() {
        CrudDao<TestEntity, String> dao = Mockito.mock(CrudDao.class);
        enableCacheTestService.setEntityFactory(new MapperEntityFactory());

        TestEntity entity = TestEntity.builder()
                .age((byte) 10)
                .enabled(true)
                .name("test")
                .build();
        entity.setId("testId");

        when(dao.query(any()))
                .then((Answer<List<TestEntity>>) invocationOnMock -> {
                    //模拟命中数据库
                    counter.incrementAndGet();
                    return new ArrayList<>(Arrays.asList(entity));
                });

        when(dao.count(any())).then((Answer<Integer>) invocationOnMock -> {
            //模拟命中数据库
            counter.incrementAndGet();
            return 1;
        });

        when(dao.update(any())).thenReturn(1);

        when(dao.delete(any())).thenReturn(1);
        when(dao.deleteByPk(anyString())).thenReturn(1);

        doNothing().when(dao).insert(anyObject());

        enableCacheTestService.setDao(dao);

        enableCacheAllEvictTestService.setDao(dao);

        enableCacheTreeTestService.setDao(dao);

        enableCacheAllEvictTreeTestService.setDao(dao);
    }

    @Test
    public void testSimpleCacheEnableService() {
        doTest(enableCacheTestService);
    }

    @Test
    public void testEnableCacheAllEvictTestService() {
        doTest(enableCacheAllEvictTestService);
    }

    @Test
    public void testEnableCacheTreeTestService() {
        doTest(enableCacheTreeTestService);
    }

    @Test
    public void testEnableCacheAllEvictTreeTestService() {
        doTest(enableCacheAllEvictTreeTestService);
    }

    public void doTest(CrudService<TestEntity, String> service) {
        service.selectByPk("testId"); //db 1
        service.selectByPk("testId");//cache
        Assert.assertEquals(counter.get(), 1);

        service.select(); //db 2
        service.select();//cache
        Assert.assertEquals(counter.get(), 2);

        service.count(); //db 3
        service.count(); //cache
        Assert.assertEquals(counter.get(), 3);

        service.updateByPk("testId", new TestEntity()); //evict cache

        service.select(); //db 4
        service.selectByPk("testId");//db 5
        service.count();//db 6

        service.select(); //cache
        service.selectByPk("testId");//cache
        service.count();//cache
        Assert.assertEquals(counter.get(), 6);

        service.deleteByPk("testId"); //evict cache
        //删除前会查询
        counter.decrementAndGet();
        service.select(); //db 7
        service.selectByPk("testId");//db 8
        service.count();//db 9

        service.select(); //cache
        service.selectByPk("testId");//cache
        service.count();//cache
        Assert.assertEquals(counter.get(), 9);

    }


}