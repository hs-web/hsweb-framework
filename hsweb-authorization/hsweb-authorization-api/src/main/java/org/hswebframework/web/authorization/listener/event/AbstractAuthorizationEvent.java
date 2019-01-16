/*
 *  Copyright 2019 http://www.hswebframework.org
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

package org.hswebframework.web.authorization.listener.event;


import org.springframework.context.ApplicationEvent;

import java.util.Optional;
import java.util.function.Function;

/**
 * 抽象授权事件,保存事件常用的数据
 *
 * @author zhouhao
 * @since 3.0
 */
public abstract class AbstractAuthorizationEvent extends ApplicationEvent implements AuthorizationEvent {
    private static final long serialVersionUID = -3027505108916079214L;

    protected String username;

    protected String password;

    private transient Function<String, Object> parameterGetter;

    /**
     * 带参构造方法,所有参数不能为null
     *
     * @param username        用户名
     * @param password        密码
     * @param parameterGetter 参数获取函数,用户获取授权时传入的参数
     */
    public AbstractAuthorizationEvent(String username, String password, Function<String, Object> parameterGetter) {
        super(username + "/" + password);
        if (username == null || password == null || parameterGetter == null) {
            throw new NullPointerException();
        }
        this.username = username;
        this.password = password;
        this.parameterGetter = parameterGetter;
    }

    @SuppressWarnings("unchecked")
    public  <T> Optional<T> getParameter(String name) {
        return Optional.ofNullable((T) parameterGetter.apply(name));
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
