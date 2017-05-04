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

package org.hswebframework.web.authorization.shiro.remember;

import org.apache.shiro.subject.PrincipalCollection;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleRememberInfo implements RememberInfo {
    private PrincipalCollection principal;
    private Map<String, Object> properties = new HashMap<>();

    private String key;

    private Long createTime;

    public SimpleRememberInfo(PrincipalCollection principal) {
        this.principal = principal;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public <T> Optional<T> getProperty(String name) {
        return Optional.ofNullable((T) properties.get(name));
    }

    @Override
    public <T> T setProperty(String name, T value) {
        return (T) properties.put(name, value);
    }

    @Override
    public PrincipalCollection getPrincipal() {
        return principal;
    }

    @Override
    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}
