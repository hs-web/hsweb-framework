package org.hswebframework.web.service.datasource.simple;

import org.hswebframework.web.commons.entity.factory.MapperEntityFactory;
import org.hswebframework.web.datasource.jta.AtomikosDataSourceConfig;
import org.hswebframework.web.entity.datasource.DataSourceConfigEntity;
import org.hswebframework.web.entity.datasource.SimpleDataSourceConfigEntity;
import org.hswebframework.web.service.datasource.DataSourceConfigService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author zhouhao
 * @since 3.0
 */
@RunWith(MockitoJUnitRunner.class)
public class InServiceJtaDataSourceRepositoryTests {

    @InjectMocks
    InServiceJtaDataSourceRepository repository = new InServiceJtaDataSourceRepository();

    @Mock
    DataSourceConfigService dataSourceConfigService;

    AtomikosDataSourceConfig config = new AtomikosDataSourceConfig();

    {
        repository.setEntityFactory(new MapperEntityFactory());
        config.setMinPoolSize(12);
        config.setMaxPoolSize(999);
        config.setId("test-ds");
        config.setName("测试");
        config.setDescribe("测试");
        config.setXaDataSourceClassName("com.alibaba.druid.DruidDataSource");
        Properties properties = new Properties();
        properties.setProperty("url", "jdbc:h2:mem:core;DB_CLOSE_ON_EXIT=FALSE");
        properties.setProperty("username", "sa");
        properties.setProperty("password", "");
        config.setXaProperties(properties);
    }

    @Before
    public void init() {
        DataSourceConfigEntity configEntity = SimpleDataSourceConfigEntity
                .builder()
                .createDate(new Date())
                .describe("测试")
                .enabled(1L)
                .name("测试")
                .build();
        Map<String, Object> properties = new HashMap<>();
        properties.put("minPoolSize", config.getMinPoolSize() + "");
        properties.put("maxPoolSize", config.getMaxPoolSize() + "");
        properties.put("borrowConnectionTimeout", config.getBorrowConnectionTimeout());
        properties.put("reapTimeout", config.getReapTimeout());
        properties.put("initTimeout", config.getInitTimeout());
        properties.put("xaDataSourceClassName", config.getXaDataSourceClassName());
        properties.put("xaProperties", config.getXaProperties());
        configEntity.setProperties(properties);
        configEntity.setId("test-ds");
        when(dataSourceConfigService.selectByPk("test-ds")).thenReturn(configEntity);
        when(dataSourceConfigService.select()).thenReturn(new ArrayList<>(Arrays.asList(configEntity)));
    }

    @Test
    public void testQuery() {
        AtomikosDataSourceConfig dataSourceConfig = repository.findById("test-ds");
        assertNotNull(dataSourceConfig);
        assertEquals(dataSourceConfig.hashCode(), config.hashCode());
    }

    @Test
    public void testQuery2() {
        AtomikosDataSourceConfig dataSourceConfig = repository.findAll().get(0);
        assertNotNull(dataSourceConfig);
        assertEquals(dataSourceConfig.hashCode(), config.hashCode());
    }

    @Test
    public void testUD() {
        try {
            repository.remove("test-ds");
            assertTrue(false);
        } catch (UnsupportedOperationException e) {

        }
        try {
            repository.add(config);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {

        }
    }

}