package org.hswebframework.web.async;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Configuration
@ConditionalOnMissingBean(AsyncJobService.class)
public class AsyncJobServiceAutoConfiguration {

    @Value("${hsweb.async.job.maxThreadPoolSize:-1}")
    private int maxThreadPoolSize = -1;

    @Bean
    @ConditionalOnMissingBean(ExecutorService.class)
    public ExecutorService executorService() {
        if (maxThreadPoolSize == -1) {
            maxThreadPoolSize = Runtime.getRuntime().availableProcessors() * 50;
        }
        return Executors.newFixedThreadPool(maxThreadPoolSize);
    }

    @Bean
    @ConditionalOnMissingBean(TransactionSupportJobWrapper.class)
    public TransactionSupportJobWrapper transactionSupportJobWrapper() {
        return new SpringTransactionSupportJobWrapper();
    }

    @Bean
    public AsyncJobService asyncJobService(ExecutorService executorService, TransactionSupportJobWrapper transactionSupportJobWrapper) {
        TransactionSupportAsyncJobService asyncJobService = new TransactionSupportAsyncJobService();

        asyncJobService.setTranslationSupportJobWrapper(transactionSupportJobWrapper);
        asyncJobService.setExecutorService(executorService);

        return asyncJobService;

    }

}
