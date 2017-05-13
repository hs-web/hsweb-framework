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

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleMessageSubscribe<T extends Message> implements MessageSubscribe<T> {
    MessageSubject subject;

    final List<Consumer<T>> consumers = new ArrayList<>();

    Queue<T> queue;

    boolean started = false;

    boolean stop = false;

    public SimpleMessageSubscribe(MessageSubject subject, Queue<T> queue) {
        this.subject = subject;
        this.queue = queue;
    }

    @Override
    public MessageSubscribe<T> onMessage(Consumer<T> consumer) {
        synchronized (consumers) {
            consumers.add(consumer);
        }
        startConsumer();
        return this;
    }

    @Override
    public void cancel() {
        stop = true;
    }

    public void startConsumer() {
        if (started) return;
        new Thread(() -> {

            while (!stop) {
                T msg = queue.poll();
                if (msg != null)
                    consumers.forEach(consumer -> consumer.accept(msg));
                else
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
        }).start();
    }
}
