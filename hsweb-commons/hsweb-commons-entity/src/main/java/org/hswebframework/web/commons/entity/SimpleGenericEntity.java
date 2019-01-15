/*
 *
 *  * Copyright 2019 http://www.hswebframework.org
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.hswebframework.web.commons.entity;

import lombok.SneakyThrows;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author zhouhao
 * @since 3.0
 */
public abstract class SimpleGenericEntity<PK> implements GenericEntity<PK> {

    private static final long serialVersionUID = 4546315942526096290L;

    private PK id;

    private Map<String, Object> properties;

    @Override
    public String toString() {
        return toString((String[]) null);
    }

    @Override
    public PK getId() {
        return this.id;
    }

    @Override
    public void setId(PK id) {
        this.id = id;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String propertyName, T defaultValue) {
        if (null == properties) {
            return defaultValue;
        }
        return (T) properties.getOrDefault(propertyName, defaultValue);
    }

    @Override
    public <T> T getProperty(String propertyName) {
        return getProperty(propertyName, null);
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        if (null == properties) {
            properties = new LinkedHashMap<>();
        }
        properties.put(propertyName, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public SimpleGenericEntity<PK> clone() {
        return (SimpleGenericEntity) super.clone();
    }
}
