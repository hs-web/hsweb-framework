/*
 *  Copyright 2016 http://www.hswebframework.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.hswebframework.web.message.builder;

import org.hswebframework.web.message.MessageSubject;
import org.hswebframework.web.message.support.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class StaticMessageSubjectBuilder {
    private static MessageSubjectBuilder messageSubjectBuilder = new SimpleMessageSubjectBuilder();

    public static UserMessageSubject user(String userId) {
        return messageSubjectBuilder.user(userId);
    }

    public static MultipleUserMessageSubject users(String... userIds) {
        return messageSubjectBuilder.users(userIds);
    }

    public static MultipleUserMessageSubject users(Set<String> userIds) {
        return messageSubjectBuilder.users(userIds);
    }

    public static MessageSubject system() {
        return messageSubjectBuilder.system();
    }

    public static TopicMessageSubject topic(String topic) {
        return messageSubjectBuilder.topic(topic);
    }

    public static QueueMessageSubject queue(String queueName) {
        return messageSubjectBuilder.queue(queueName);
    }

    public static MultipleQueueMessageSubject queues(String... queueNames) {
        return messageSubjectBuilder.queues(queueNames);
    }

    public static MultipleQueueMessageSubject queues(Set<String> queueNames) {
        return messageSubjectBuilder.queues(queueNames);
    }

}
