package org.hswebframework.web.service;

import org.hswebframework.web.commons.entity.events.EntityModifyEvent;
import org.hswebframework.web.commons.entity.factory.MapperEntityFactory;
import org.hswebframework.web.dao.CrudDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;

/**
 * @author zhouhao
 * @since 3.0
 */
@SpringBootTest(classes = SpringTestApplication.class)
@RunWith(value = SpringRunner.class)
@Component
public class ModifyEventTests {

    @Autowired
    private TestModifyEntityService modifyEntityService;

    private AtomicInteger counter = new AtomicInteger();

    @Before
    public void init() {
        CrudDao<TestModifyEntity, String> dao = Mockito.mock(CrudDao.class);
        modifyEntityService.setEntityFactory(new MapperEntityFactory());
        modifyEntityService.setDao(dao);
        TestModifyEntity entity = TestModifyEntity.builder()
                .age((byte) 10)
                .enabled(true)
                .name("test")
                .build();
        entity.setId("testId");

        when(dao.query(any()))
                .then((Answer<List<TestModifyEntity>>) invocationOnMock -> {
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

    }


    @Test
    public void modifyTest() {
        TestModifyEntity entity = new TestModifyEntity();
        entity.setId("testId");
        entity.setAge((byte) 10);

        modifyEntityService.updateByPk("test", entity);
        Assert.assertTrue(TestModifyEntityService.eventCounter.get() != 0);
    }


}