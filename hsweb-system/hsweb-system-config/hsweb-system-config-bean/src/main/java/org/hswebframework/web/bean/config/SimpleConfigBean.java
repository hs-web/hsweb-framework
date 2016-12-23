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

package org.hswebframework.web.bean.config;


import org.hswebframework.web.commons.beans.SimpleGenericBean;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleConfigBean extends SimpleGenericBean<String> implements ConfigBean {

    //备注
    private String remark;

    //配置内容
    private List<ConfigContent> content;

    //创建日期
    @NotNull
    private java.util.Date createDate;

    //最后一次修改日期
    private java.util.Date updateDate;

    //配置分类ID
    private String classifiedId;

    @NotNull
    private String creatorId;

    private Map<String, ConfigContent> cache;

    @Override
    public String getCreatorId() {
        return creatorId;
    }

    public ConfigBean setCreatorId(String creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    @Override
    public String getRemark() {
        return remark;
    }

    @Override
    public ConfigBean setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    @Override
    public List<ConfigContent> getContent() {
        return content;
    }

    @Override
    public ConfigBean addContent(String key, Object value, String comment) {
        if (content == null) content = new ArrayList<>();
        content.add(new SimpleConfigContent(key, value, comment));
        return this;
    }

    @Override
    public ConfigContent get(String key) {
        if (cache == null) {
            synchronized (this) {
                if (cache == null) {
                    if (content == null || content.isEmpty()) cache = new HashMap<>();
                    cache = content.stream()
                            .collect(Collectors.toMap(ConfigContent::getKey, content -> content));
                }
            }
        }
        return cache.get(key);
    }

    @Override
    public ConfigBean setContent(List<ConfigContent> content) {
        this.content = content;
        return this;
    }

    @Override
    public Date getCreateDate() {
        return createDate;
    }

    @Override
    public ConfigBean setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    @Override
    public Date getUpdateDate() {
        return updateDate;
    }

    @Override
    public ConfigBean setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
        return this;
    }

    @Override
    public String getClassifiedId() {
        return classifiedId;
    }

    @Override
    public ConfigBean setClassifiedId(String classifiedId) {
        this.classifiedId = classifiedId;
        return this;
    }

    @Override
    public SimpleConfigBean cloneBean() {
        // TODO: 16-12-23  完成克隆代码
        return null;
    }

    public static class SimpleConfigContent implements ConfigContent {
        private String key;

        private Object value;

        private String comment;

        public SimpleConfigContent() {
        }

        public SimpleConfigContent(String key, Object value, String comment) {
            this.key = key;
            this.value = value;
            this.comment = comment;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}
