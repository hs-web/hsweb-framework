/*
 *
 *  * Copyright 2016 http://www.hswebframework.org
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

package org.hswebframework.web.controller.message;


import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 响应消息。controller中处理后，返回此对象，响应请求结果给客户端。
 */
@ApiModel(description = "响应结果")
public class ResponseMessage<T> implements Serializable {
    private static final long serialVersionUID = 8992436576262574064L;

    protected String message;

    protected T result;

    protected int status;

    private Long timestamp;

    @ApiModelProperty("调用结果消息")
    public String getMessage() {
        return message;
    }

    @ApiModelProperty(value = "状态码", required = true)
    public int getStatus() {
        return status;
    }

    @ApiModelProperty("成功时响应数据")
    public T getResult() {
        return result;
    }

    @ApiModelProperty(value = "时间戳", required = true, dataType = "Long")
    public Long getTimestamp() {
        return timestamp;
    }

    public static <T> ResponseMessage<T> error(String message) {
        return error(500, message);
    }

    public static <T> ResponseMessage<T> error(int status, String message) {
        ResponseMessage<T> msg = new ResponseMessage<>();
        msg.message = message;
        msg.status(status);
        return msg.putTimeStamp();
    }

    public static <T> ResponseMessage<T> ok() {
        return ok(null);
    }

    private ResponseMessage<T> putTimeStamp() {
        this.timestamp = System.currentTimeMillis();
        return this;
    }

    public static <T> ResponseMessage<T> ok(T result) {
        return new ResponseMessage<T>()
                .result(result)
                .putTimeStamp()
                .status(200);
    }

    public ResponseMessage<T> result(T result) {
        this.result = result;
        return this;
    }

    /**
     * 过滤字段：指定需要序列化的字段
     */
    private transient Map<Class<?>, Set<String>> includes;

    /**
     * 过滤字段：指定不需要序列化的字段
     */
    private transient Map<Class<?>, Set<String>> excludes;

    public ResponseMessage() {

    }

    public ResponseMessage<T> include(Class<?> type, String... fields) {
        return include(type, Arrays.asList(fields));
    }

    public ResponseMessage<T> include(Class<?> type, Collection<String> fields) {
        if (includes == null)
            includes = new HashMap<>();
        if (fields == null || fields.isEmpty()) return this;
        fields.forEach(field -> {
            if (field.contains(".")) {
                String tmp[] = field.split("[.]", 2);
                try {
                    Field field1 = type.getDeclaredField(tmp[0]);
                    if (field1 != null) {
                        include(field1.getType(), tmp[1]);
                    }
                } catch (Throwable e) {
                }
            } else {
                getStringListFromMap(includes, type).add(field);
            }
        });
        return this;
    }

    public ResponseMessage<T> exclude(Class type, Collection<String> fields) {
        if (excludes == null)
            excludes = new HashMap<>();
        if (fields == null || fields.isEmpty()) return this;
        fields.forEach(field -> {
            if (field.contains(".")) {
                String tmp[] = field.split("[.]", 2);
                try {
                    Field field1 = type.getDeclaredField(tmp[0]);
                    if (field1 != null) {
                        exclude(field1.getType(), tmp[1]);
                    }
                } catch (Throwable e) {
                }
            } else {
                getStringListFromMap(excludes, type).add(field);
            }
        });
        return this;
    }

    public ResponseMessage<T> exclude(Collection<String> fields) {
        if (excludes == null)
            excludes = new HashMap<>();
        if (fields == null || fields.isEmpty()) return this;
        Class type;
        if (getResult() != null) type = getResult().getClass();
        else return this;
        exclude(type, fields);
        return this;
    }

    public ResponseMessage<T> include(Collection<String> fields) {
        if (includes == null)
            includes = new HashMap<>();
        if (fields == null || fields.isEmpty()) return this;
        Class type;
        if (getResult() != null) type = getResult().getClass();
        else return this;
        include(type, fields);
        return this;
    }

    public ResponseMessage<T> exclude(Class type, String... fields) {
        return exclude(type, Arrays.asList(fields));
    }

    public ResponseMessage<T> exclude(String... fields) {
        return exclude(Arrays.asList(fields));
    }

    public ResponseMessage<T> include(String... fields) {
        return include(Arrays.asList(fields));
    }

    protected Set<String> getStringListFromMap(Map<Class<?>, Set<String>> map, Class type) {
        return map.computeIfAbsent(type, k -> new HashSet<>());
    }

    @Override
    public String toString() {
        return JSON.toJSONStringWithDateFormat(this, "yyyy-MM-dd HH:mm:ss");
    }

    public ResponseMessage<T> status(int status) {
        this.status = status;
        return this;
    }

    @ApiModelProperty(hidden = true)
    public Map<Class<?>, Set<String>> getExcludes() {
        return excludes;
    }

    @ApiModelProperty(hidden = true)
    public Map<Class<?>, Set<String>> getIncludes() {
        return includes;
    }

}