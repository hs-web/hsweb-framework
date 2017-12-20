package org.hswebframework.web.async;

/**
 * 异步任务服务
 *
 * @author zhouhao
 */
public interface AsyncJobService {
    /**
     * @return 创建一个异步任务容器
     */
    BatchAsyncJobContainer batch();
}
