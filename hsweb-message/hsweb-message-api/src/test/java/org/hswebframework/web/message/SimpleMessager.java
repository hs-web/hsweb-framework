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

package org.hswebframework.web.message;

import org.hswebframework.web.message.support.TextMessage;
import org.hswebframework.web.message.support.UserMessageSubject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author zhouhao
 */
public class SimpleMessager implements Messager {

    Map<String, Queue<Message>> queueStorage = new ConcurrentHashMap<>(256);

    private Queue<Message> getQueue(String key) {
        return queueStorage.computeIfAbsent(key, k -> new LinkedBlockingQueue<>());
    }

    List<MessagePublishHandler> publishHanlders = new ArrayList<>();

    public SimpleMessager() {
        //just support TextMessage
        publishHanlders.add(new MessagePublishHandler() {
            @Override
            public boolean isSupport(Message message) {
                return message instanceof TextMessage;
            }

            @Override
            public MessagePublish handle(Message message) {
                return new SimpleMessagePublish() {
                    @Override
                    public void send() {
                        getQueue(buildKey(to)).offer(message);
                    }
                };
            }
        });
    }

    public String buildKey(MessageSubject subject) {
        if (subject instanceof UserMessageSubject) {
            return UserMessageSubject.class.getName().concat(((UserMessageSubject) subject).getUserId());
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public MessagePublish publish(Message message) {
        return publishHanlders.stream()
                .filter(handler -> handler.isSupport(message))
                .findFirst()
                .orElseThrow(UnsupportedOperationException::new)
                .handle(message);
    }

    @Override
    public <M extends Message> MessageSubscribe<M> subscribe(MessageSubject subscribe) {
        return new SimpleMessageSubscribe(subscribe, getQueue(buildKey(subscribe)));
    }

}
