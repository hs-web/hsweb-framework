package org.hswebframework.web.service.datasource.simple;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.datasource.DynamicDataSource;
import org.hswebframework.web.datasource.DynamicDataSourceProxy;
import org.hswebframework.web.datasource.annotation.UseDefaultDataSource;
import org.hswebframework.web.datasource.config.DynamicDataSourceConfigRepository;
import org.hswebframework.web.datasource.service.AbstractDynamicDataSourceService;
import org.hswebframework.web.datasource.service.DataSourceCache;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class InDBDynamicDataSourceService extends AbstractDynamicDataSourceService<InDBDynamicDataSourceConfig> {

    public InDBDynamicDataSourceService(DynamicDataSourceConfigRepository<InDBDynamicDataSourceConfig> repository,
                                        DynamicDataSource defaultDataSource) {

        super(repository, defaultDataSource);
    }

    ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Override
    public DynamicDataSource getDataSource(String dataSourceId) {
        try {
            DataSourceHolder.switcher().useDefault();
            return super.getDataSource(dataSourceId);
        } finally {
            DataSourceHolder.switcher().useLast();
        }
    }

    protected void closeDataSource(DataSource dataSource) {
        if (null == dataSource) {
            return;
        }
        try {
            if (dataSource instanceof Closeable) {
                ((Closeable) dataSource).close();
            } else {
                Method closeMethod = ReflectionUtils.findMethod(dataSource.getClass(), "close");
                if (closeMethod != null) {
                    ReflectionUtils.invokeMethod(closeMethod, dataSource);
                }
            }
        } catch (Exception e) {
            log.warn("关闭数据源[{}]失败", dataSource, e);
        }
    }

    @Override
    @SneakyThrows
    protected DataSourceCache createCache(InDBDynamicDataSourceConfig config) {
        if (config.getProperties() == null) {
            throw new UnsupportedOperationException("配置不存在");
        }

        CountDownLatch initCountDownLatch = new CountDownLatch(1);
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        FastBeanCopier.copy(config.getProperties(), dataSourceProperties);
        AtomicReference<DataSource> dataSourceReference = new AtomicReference<>();
        AtomicBoolean closed = new AtomicBoolean();
        AtomicBoolean success = new AtomicBoolean();
        int initTimeOut = Integer.parseInt(String.valueOf(config.getProperties().getOrDefault("InitTimeout", "20")));

        executorService.submit(() -> {
            try {
                DataSource dataSource = dataSourceProperties
                        .initializeDataSourceBuilder()
                        .build();
                dataSourceReference.set(dataSource);
                FastBeanCopier.copy(config.getProperties(), dataSource);
                //test datasource init success
                dataSource.getConnection().close();
                if (closed.get()) {
                    closeDataSource(dataSource);
                } else {
                    success.set(true);
                }
            } catch (Exception e) {
                log.warn("初始化数据源[{}]失败", config.getId(), e);
            } finally {
                initCountDownLatch.countDown();
            }
        });

        try {
            @SuppressWarnings("all")
            boolean waitSuccess = initCountDownLatch.await(initTimeOut, TimeUnit.SECONDS);
        } catch (@SuppressWarnings("all") InterruptedException ignore) {
            //ignore
        }
        if (!success.get()) {
            closed.set(true);
            closeDataSource(dataSourceReference.get());
            throw new BusinessException("初始化数据源[" + config.getId() + "]失败");
        }
        return new DataSourceCache(
                config.getProperties().hashCode(),
                new DynamicDataSourceProxy(config.getId(), dataSourceReference.get()),
                initCountDownLatch, config) {
            @Override
            public void closeDataSource() {
                super.closeDataSource();
                closed.set(true);
                InDBDynamicDataSourceService.this.closeDataSource(getDataSource().getNative());
            }
        };

    }
}
