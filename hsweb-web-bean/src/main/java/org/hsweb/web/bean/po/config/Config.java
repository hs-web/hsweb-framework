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

package org.hsweb.web.bean.po.config;

import com.alibaba.fastjson.JSON;
import org.hsweb.web.bean.po.GenericPo;
import org.hsweb.web.bean.po.classified.Classified;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置实体
 */
public class Config extends GenericPo<String> {

    private static final long serialVersionUID = 5328848488856425388L;

    //备注
    private String remark;

    //配置内容
    private String content;

    //创建日期
    private java.util.Date createDate;

    //最后一次修改日期
    private java.util.Date updateDate;

    //配置分类ID
    private String classifiedId;

    /**
     * 获取 备注
     *
     * @return String 备注
     */
    public String getRemark() {
        return this.remark;
    }

    /**
     * 设置 备注
     *
     * @param remark 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 获取 配置内容
     *
     * @return String 配置内容
     */
    public String getContent() {
        return this.content;
    }

    /**
     * 设置 配置内容
     *
     * @param content 配置内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取 创建日期
     *
     * @return {@link Date} 创建日期
     */
    public Date getCreateDate() {
        return this.createDate;
    }

    /**
     * 设置 创建日期
     *
     * @param createDate 创建日期
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * 获取 最后一次修改日期
     *
     * @return java.util.Date 最后一次修改日期
     */
    public Date getUpdateDate() {
        return this.updateDate;
    }

    /**
     * 设置 最后一次修改日期
     *
     * @param updateDate 最后一次修改日期
     */
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }


    /**
     * 获取分类ID {@link Classified#getId()}
     *
     * @return 分类ID
     */
    public String getClassifiedId() {
        return classifiedId;
    }

    /**
     * 设置分类ID
     *
     * @param classifiedId 分类ID
     */
    public void setClassifiedId(String classifiedId) {
        this.classifiedId = classifiedId;
    }

    /**
     * 将配置(json)转为map,如果配置内容不为map结构({"key":"value"})
     * 则使用array[map]方式转换,将array里的map的key的值作为key。value的值作为value。
     * 比如: 配置内容为[{"key":"1","value":"男"},{"key":"0","value":"女"}].转为map后，则为{"1":"男","2":"女"}
     *
     * @return 转换的结果
     * @since 1.0
     */
    public Map<Object, Object> toMap() {
        if (getContent().trim().startsWith("{")) {
            return JSON.parseObject(getContent(), Map.class);
        }
        Map<Object, Object> data = new LinkedHashMap<>();
        toList().forEach(map -> data.put(map.get("key"), map.get("value")));
        return data;
    }

    /**
     * 转为list结构,(如果配置内容结构不为json。将可能抛出异常{@link com.alibaba.fastjson.JSONException})
     *
     * @return list结构
     * @throws com.alibaba.fastjson.JSONException 解析配置内容错误
     */
    public List<Map<Object, Object>> toList() {
        List<Map<Object, Object>> array = (List) JSON.parseArray(getContent(), Map.class);
        return array;
    }

    public interface Property extends GenericPo.Property {
        /**
         * @see Config#remark
         */
        String remark           = "remark";
        /**
         * @see Config#content
         */
        String content          = "content";
        /**
         * @see Config#createDate
         */
        String createDate       = "createDate";
        /**
         * @see Config#updateDate
         */
        String updateDate       = "updateDate";
        /**
         * @see Config#classifiedId
         */
        String classifiedId     = "classifiedId";
    }
}