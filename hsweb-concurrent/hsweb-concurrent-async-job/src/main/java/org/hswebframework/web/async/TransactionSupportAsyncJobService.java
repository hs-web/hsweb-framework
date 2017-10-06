package org.hswebframework.web.async;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;

/**
 * @author zhouhao
 */
public class TransactionSupportAsyncJobService implements AsyncJobService {

    private ExecutorService executorService;

    private TransactionSupportJobWrapper translationSupportJobWrapper;

    @Autowired(required = false)
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Autowired
    public void setTranslationSupportJobWrapper(TransactionSupportJobWrapper translationSupportJobWrapper) {
        this.translationSupportJobWrapper = translationSupportJobWrapper;
    }

    @Override
    public BatchAsyncJobContainer batch() {
        return new TransactionBatchAsyncJobContainer(executorService, translationSupportJobWrapper);
    }
}
