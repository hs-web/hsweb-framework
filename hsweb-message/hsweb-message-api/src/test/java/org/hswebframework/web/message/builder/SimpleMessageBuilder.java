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

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleMessageBuilder implements MessageBuilder {
    @Override
    public TextMessage text(String msg) {
        return new TextMessage() {
            @Override
            public String getMessage() {
                return msg;
            }

            @Override
            public String toString() {
                return msg;
            }
        };
    }

    @Override
    public <T> ObjectMessage object(T msg) {
        return (ObjectMessage) () -> msg;
    }

    @Override
    public DataMessage data(byte[] msg) {
        return (DataMessage) () -> msg;
    }

    @Override
    public ServiceInvokerMessage service(String serviceName) {

        return null;
    }
}
