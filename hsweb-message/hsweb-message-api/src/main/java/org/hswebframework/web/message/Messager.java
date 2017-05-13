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

/**
 * simple
 * <pre>
 *     messager
 *     .publish(text("hello"))
 *     .to(user("admin"))
 *     .send();
 * </pre>
 * send object to topic
 * <pre>
 *     messager
 *     .publish(object(user))
 *     .to(topic("user-login"))
 *     .send();
 * </pre>
 * subscribe topic
 * <pre>
 *     messager
 *     .subscribe(topic("user-login"))
 *     .iam(user("admin"))
 *     .onMessage(user->System.out.println(user));
 * </pre>
 * subscribe user msg
 * <pre>
 *      messager
 *     .subscribe(user("admin"))
 *     .onMessage(message->System.out.println(message));
 * </pre>
 *
 * @author zhouhao
 * @since 3.0
 */
public interface Messager {
    MessagePublish publish(Message message);

    <M extends Message> MessageSubscribe<M> subscribe(MessageSubject subscribe);
}
