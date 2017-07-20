package org.hswebframework.web.message.support;

import org.hswebframework.web.message.MessageSubject;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface QueueMessageSubject extends MessageSubject {
    String getQueueName();
}
