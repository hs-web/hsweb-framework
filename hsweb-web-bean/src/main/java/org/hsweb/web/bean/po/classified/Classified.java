/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.bean.po.classified;

import org.hsweb.web.bean.po.GenericPo;

/**
 * 资源分类实体，用于对系统中各种类型的资源进行分类
 *
 * @author zhouhao admin@hsweb.me
 * @since 1.0
 */
public class Classified extends GenericPo<String> {
    //分类名称
    private String name;
    //备注
    private String remark;
    //分类类型
    private String type;
    //父级分类
    private String parentId;
    //显示图标
    private String icon;
    //其他配置
    private String config;
    //排序
    private int sortIndex;

    /**
     * 获取 分类名称
     *
     * @return java.lang.String 分类名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置 分类名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取 备注
     *
     * @return java.lang.String 备注
     */
    public String getRemark() {
        return this.remark;
    }

    /**
     * 设置 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 获取 分类类型
     *
     * @return java.lang.String 分类类型
     */
    public String getType() {
        return this.type;
    }

    /**
     * 设置 分类类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取 父级分类
     *
     * @return java.lang.String 父级分类
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * 设置 父级分类
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * 获取 显示图标
     *
     * @return java.lang.String 显示图标
     */
    public String getIcon() {
        return this.icon;
    }

    /**
     * 设置 显示图标
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * 获取 其他配置
     *
     * @return java.lang.String 其他配置
     */
    public String getConfig() {
        return this.config;
    }

    /**
     * 设置 其他配置
     */
    public void setConfig(String config) {
        this.config = config;
    }

    /**
     * 获取 排序
     *
     * @return int 排序
     */
    public int getSortIndex() {
        return sortIndex;
    }

    /**
     * 设置 排序
     */
    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }



public interface Property extends GenericPo.Property{
	/**
	 *
	 * @see Classified#name
	 */
	String name="name";
	/**
	 *
	 * @see Classified#remark
	 */
	String remark="remark";
	/**
	 *
	 * @see Classified#type
	 */
	String type="type";
	/**
	 *
	 * @see Classified#parentId
	 */
	String parentId="parentId";
	/**
	 *
	 * @see Classified#icon
	 */
	String icon="icon";
	/**
	 *
	 * @see Classified#config
	 */
	String config="config";
	/**
	 *
	 * @see Classified#sortIndex
	 */
	String sortIndex="sortIndex";
	}
}