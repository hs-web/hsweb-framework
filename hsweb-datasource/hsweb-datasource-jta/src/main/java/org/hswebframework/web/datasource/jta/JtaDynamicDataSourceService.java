package org.hswebframework.web.datasource.jta;

import lombok.SneakyThrows;
import org.hswebframework.web.datasource.DynamicDataSource;
import org.hswebframework.web.datasource.DynamicDataSourceProxy;
import org.hswebframework.web.datasource.config.DynamicDataSourceConfigRepository;
import org.hswebframework.web.datasource.exception.DataSourceNotFoundException;
import org.hswebframework.web.datasource.service.AbstractDynamicDataSourceService;
import org.hswebframework.web.datasource.service.DataSourceCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhouhao
 */
public class JtaDynamicDataSourceService extends AbstractDynamicDataSourceService<AtomikosDataSourceConfig> {

    private Executor executor = Executors.newFixedThreadPool(4);

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public JtaDynamicDataSourceService(DynamicDataSourceConfigRepository<AtomikosDataSourceConfig> repository, DynamicDataSource defaultDataSource) {
        super(repository, defaultDataSource);
    }

    public JtaDynamicDataSourceService(DynamicDataSourceConfigRepository<AtomikosDataSourceConfig> repository, DataSource dataSource) throws SQLException {
        super(repository, dataSource);
    }

    @Autowired(required = false)
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }


    @Override
    @SneakyThrows
    protected DataSourceCache createCache(AtomikosDataSourceConfig config) {
        AtomikosDataSourceBean atomikosDataSourceBean = new AtomikosDataSourceBean();
        config.putProperties(atomikosDataSourceBean);
        atomikosDataSourceBean.setBeanName("dynamic_ds_" + config.getId());
        atomikosDataSourceBean.setUniqueResourceName("dynamic_ds_" + config.getId());
        AtomicInteger successCounter = new AtomicInteger();
        CountDownLatch downLatch = new CountDownLatch(1);
        DataSourceCache cache = new DataSourceCache(config.hashCode(), new DynamicDataSourceProxy(config.getId(), atomikosDataSourceBean), downLatch, config) {
            @Override
            public void closeDataSource() {
                super.closeDataSource();
                atomikosDataSourceBean.close();
                XADataSource dataSource = atomikosDataSourceBean.getXaDataSource();
                if (dataSource instanceof Closeable) {
                    try {
                        ((Closeable) dataSource).close();
                    } catch (IOException e) {
                        logger.error("close xa datasource error", e);
                    }
                } else {
                    logger.warn("XADataSource is not instanceof Closeable!", (Object) Thread.currentThread().getStackTrace());
                }
            }
        };
        //异步初始化
        executor.execute(() -> {
            try {
                atomikosDataSourceBean.init();
                successCounter.incrementAndGet();
                downLatch.countDown();
            } catch (Exception e) {
                logger.error("init datasource {} error", config.getId(), e);

                //atomikosDataSourceBean.close();
            }
        });
        //初始化状态判断
        executor.execute(() -> {
            try {
                Thread.sleep(config.getInitTimeout() * 1000L);
            } catch (InterruptedException ignored) {
                logger.warn(ignored.getMessage(), ignored);
                Thread.currentThread().interrupt();
            } finally {
                if (successCounter.get() == 0) {
                    // 初始化超时,认定为失败
                    logger.error("init timeout ({}ms)", config.getInitTimeout());
                    cache.closeDataSource();
                    if (downLatch.getCount() > 0) {
                        downLatch.countDown();
                    }
                }
            }
        });
        return cache;
    }
}
