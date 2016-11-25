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
package org.hsweb.web.bean.po.plan;

import org.hibernate.validator.constraints.NotBlank;
import org.hsweb.web.bean.po.GenericPo;

/**
 * 查询方案
 * Created by hsweb-generator Aug 8, 2016 1:24:08 AM
 */
public class QueryPlan extends GenericPo<String> {
    //方案名称
    @NotBlank(message = "名称不能为空")
    private String name;
    //方案分类
    @NotBlank(message = "类型不能为空")
    private String type;
    //方案配置
    private String config;
    //是否共享方案
    private boolean sharing;
    //创建人ID
    @NotBlank(message = "创建人不能为空")
    private String creatorId;
    //创建日期
    private java.util.Date createDate;

    /**
     * 获取 方案名称
     *
     * @return String 方案名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置 方案名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取 方案分类
     *
     * @return String 方案分类
     */
    public String getType() {
        return this.type;
    }

    /**
     * 设置 方案分类
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取 方案配置
     *
     * @return String 方案配置
     */
    public String getConfig() {
        return this.config;
    }

    /**
     * 设置 方案配置
     */
    public void setConfig(String config) {
        this.config = config;
    }

    /**
     * 获取 是否共享方案
     *
     * @return boolean 是否共享方案
     */
    public boolean isSharing() {
        return this.sharing;
    }

    /**
     * 设置 是否共享方案
     */
    public void setSharing(boolean sharing) {
        this.sharing = sharing;
    }

    /**
     * 获取 创建人ID
     *
     * @return String 创建人ID
     */
    public String getCreatorId() {
        return this.creatorId;
    }

    /**
     * 设置 创建人ID
     */
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * 获取 创建日期
     *
     * @return java.util.Date 创建日期
     */
    public java.util.Date getCreateDate() {
        return this.createDate;
    }

    /**
     * 设置 创建日期
     */
    public void setCreateDate(java.util.Date createDate) {
        this.createDate = createDate;
    }



public interface Property extends GenericPo.Property{
	/**
	 *
	 * @see QueryPlan#name
	 */
	String name="name";
	/**
	 *
	 * @see QueryPlan#type
	 */
	String type="type";
	/**
	 *
	 * @see QueryPlan#config
	 */
	String config="config";
	/**
	 *
	 * @see QueryPlan#sharing
	 */
	String sharing="sharing";
	/**
	 *
	 * @see QueryPlan#creatorId
	 */
	String creatorId="creatorId";
	/**
	 *
	 * @see QueryPlan#createDate
	 */
	String createDate="createDate";
	}
}