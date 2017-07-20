package org.hswebframework.web.datasource.jta;

import org.hswebframework.web.datasource.DynamicDataSource;
import org.hswebframework.web.datasource.DynamicDataSourceProxy;
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
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class JtaDynamicDataSourceService extends AbstractDynamicDataSourceService {

    private JtaDataSourceRepository jtaDataSourceRepository;

    private Executor executor = Executors.newCachedThreadPool();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired(required = false)
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public JtaDynamicDataSourceService(JtaDataSourceRepository jtaDataSourceRepository, DynamicDataSource defaultDataSource) {
        super(defaultDataSource);
        this.jtaDataSourceRepository = jtaDataSourceRepository;
    }

    public JtaDynamicDataSourceService(JtaDataSourceRepository jtaDataSourceRepository, DataSource dataSource) throws SQLException {
        super(dataSource);
        this.jtaDataSourceRepository = jtaDataSourceRepository;
    }

    @Override
    protected int getHash(String id) {
        AtomikosDataSourceConfig config = jtaDataSourceRepository.getConfig(id);
        if (null == config) return 0;
        return config.hashCode();
    }

    @Override
    protected DataSourceCache createCache(String id) {
        AtomikosDataSourceConfig config = jtaDataSourceRepository.getConfig(id);
        if (config == null) {
            throw new DataSourceNotFoundException(id);
        }
        AtomikosDataSourceBean atomikosDataSourceBean = new AtomikosDataSourceBean();
        config.putProperties(atomikosDataSourceBean);
        atomikosDataSourceBean.setBeanName("dynamic_ds_" + id);
        atomikosDataSourceBean.setUniqueResourceName("dynamic_ds_" + id);
        AtomicInteger successCounter = new AtomicInteger();
        CountDownLatch downLatch = new CountDownLatch(1);
        try {
            DataSourceCache cache = new DataSourceCache(config.hashCode(), new DynamicDataSourceProxy(id, atomikosDataSourceBean), downLatch) {
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
                    logger.error("init datasource {} error", id, e);
                    //atomikosDataSourceBean.close();
                }
            });
            //初始化状态判断
            executor.execute(() -> {
                try {
                    Thread.sleep(config.getInitTimeout() * 1000);
                } catch (InterruptedException ignored) {
                } finally {
                    if (successCounter.get() == 0) {
                        // 初始化超时,认定为失败
                        logger.error("init timeout ({}ms)", config.getInitTimeout());
                        cache.closeDataSource();
                        if (downLatch.getCount() > 0)
                            downLatch.countDown();
                    }
                }
            });
            return cache;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
