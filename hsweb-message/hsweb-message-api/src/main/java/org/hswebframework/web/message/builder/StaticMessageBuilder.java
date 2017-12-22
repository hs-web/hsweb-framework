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

import org.hswebframework.web.message.support.DataMessage;
import org.hswebframework.web.message.support.ObjectMessage;
import org.hswebframework.web.message.support.ServiceInvokerMessage;
import org.hswebframework.web.message.support.TextMessage;

import java.io.Serializable;

/**
 * @author zhouhao
 */
public class StaticMessageBuilder {
    private static MessageBuilder messageBuilder = new SimpleMessageBuilder();

    public static TextMessage text(String msg) {
        return messageBuilder.text(msg);
    }

    public static <T extends Serializable> ObjectMessage<T> object(T msg) {
        return messageBuilder.object(msg);
    }

    public static DataMessage data(byte[] msg) {
        return messageBuilder.data(msg);
    }

    public static ServiceInvokerMessage service(String serviceName, String method, Serializable... args) {
        return messageBuilder.service(serviceName, method, args);
    }
}
