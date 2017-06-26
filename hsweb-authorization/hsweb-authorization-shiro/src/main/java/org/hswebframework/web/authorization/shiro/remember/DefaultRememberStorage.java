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
import org.hswebframework.web.id.IDGenerator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhao
 */
public class DefaultRememberStorage implements RememberStorage {
    private Map<String, RememberInfo> storage = new ConcurrentHashMap<>(256);

    @Override
    public RememberInfo create(PrincipalCollection collection) {
        SimpleRememberInfo info = new SimpleRememberInfo(collection);
        info.setCreateTime(System.currentTimeMillis());
        info.setKey(IDGenerator.MD5.generate());
        return info;
    }

    @Override
    public RememberInfo get(String key) {
        return storage.get(key);
    }

    @Override
    public RememberInfo remove(String key) {
        return storage.remove(key);
    }

    @Override
    public void put(RememberInfo rememberInfo) {
        storage.put(rememberInfo.getKey(), rememberInfo);
    }
}
