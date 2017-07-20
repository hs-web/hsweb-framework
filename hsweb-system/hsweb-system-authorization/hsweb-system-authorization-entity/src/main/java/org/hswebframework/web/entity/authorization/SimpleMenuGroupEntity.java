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
 */
package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.SimpleTreeSortSupportEntity;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 菜单分组
 *
 * @author hsweb-generator-online
 */
public class SimpleMenuGroupEntity extends SimpleTreeSortSupportEntity<String> implements MenuGroupEntity {
    //分组名称
    private String  name;
    //分组描述
    private String  describe;
    //是否默认
    private Boolean defaultGroup;
    //状态
    private Byte    status;

    private List<SimpleMenuGroupEntity> children;

    private List<SimpleMenuGroupBindEntity> bindInfo;

    /**
     * @return 分组名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置 分组名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return 分组描述
     */
    public String getDescribe() {
        return this.describe;
    }

    /**
     * 设置 分组描述
     */
    public void setDescribe(String describe) {
        this.describe = describe;
    }

    /**
     * @return 是否默认
     */
    public Boolean isDefaultGroup() {
        return this.defaultGroup;
    }

    /**
     * 设置 是否默认
     */
    public void setDefaultGroup(Boolean defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    @Override
    public List<SimpleMenuGroupEntity> getChildren() {
        return children;
    }

    public void setChildren(List<SimpleMenuGroupEntity> children) {
        this.children = children;
    }

    @Override
    public List<MenuGroupBindEntity> getBindInfo() {
        if (bindInfo == null)
            return Collections.emptyList();
        return new LinkedList<>(bindInfo);
    }

    @Override
    public void setBindInfo(List<MenuGroupBindEntity> bindInfo) {
        this.bindInfo = new LinkedList(bindInfo);
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }
}