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

package org.hswebframework.web.authorization.listener;

import org.hswebframework.web.authorization.listener.event.AuthorizationEvent;

import java.util.*;

/**
 * @author zhouhao
 */
public class AuthorizationListenerDispatcher {

    private Map<Class<? extends AuthorizationEvent>, List<AuthorizationListener>> listenerStore = new HashMap<>();

    public <E extends AuthorizationEvent> void addListener(Class<E> eventClass, AuthorizationListener<E> listener) {
        listenerStore.computeIfAbsent(eventClass, (k) -> new LinkedList<>())
                .add(listener);
    }

    @SuppressWarnings("unchecked")
    public <E extends AuthorizationEvent> int doEvent(Class<E> eventType, E event) {
        List<AuthorizationListener<E>> store = (List) listenerStore.get(eventType);
        if (null != store) {
            store.forEach(listener -> listener.on(event));
            return store.size();
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    public <E extends AuthorizationEvent> int doEvent(E event) {
        return doEvent((Class<E>) event.getClass(), event);
    }
}
