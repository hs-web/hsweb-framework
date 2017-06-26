package org.hswebframework.web.message.support;

import org.hswebframework.web.message.MessageSubject;

import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface MultipleQueueMessageSubject extends MessageSubject {
    Set<String> getQueueName();
}
