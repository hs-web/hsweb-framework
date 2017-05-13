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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleMessageSubjectBuilder implements MessageSubjectBuilder, Serializable {
    @Override
    public UserMessageSubject user(String userId) {
        return () -> userId;
    }

    @Override
    public MultipleUserMessageSubject users(String... userIds) {
        return (MultipleUserMessageSubject) () -> new HashSet<>(Arrays.asList(userIds));
    }

    @Override
    public MultipleUserMessageSubject users(Set<String> userIds) {
        return (MultipleUserMessageSubject) () -> userIds;
    }

    @Override
    public MessageSubject system() {
        return null;
    }

    @Override
    public TopicMessageSubject topic(String topic) {
        return () -> topic;
    }

    @Override
    public QueueMessageSubject queue(String queueName) {
        return () -> queueName;
    }

    @Override
    public MultipleQueueMessageSubject queues(String... userIds) {
        Set<String> ids = Arrays.stream(userIds).collect(Collectors.toSet());
        return queues(ids);
    }

    @Override
    public MultipleQueueMessageSubject queues(Set<String> userIds) {
        return () -> userIds;
    }
}
