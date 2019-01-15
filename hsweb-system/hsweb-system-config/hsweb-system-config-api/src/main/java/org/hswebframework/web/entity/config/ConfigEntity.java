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

package org.hswebframework.web.entity.config;


import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.commons.entity.RecordCreationEntity;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface ConfigEntity extends GenericEntity<String>,RecordCreationEntity {

    /**
     * 获取 备注
     *
     * @return String 备注
     */
    String getRemark();

    /**
     * 设置 备注
     *
     * @param remark 备注
     */
    void setRemark(String remark);

    /**
     * 获取 配置内容
     *
     * @return String 配置内容
     */
    List<ConfigContent> getContent();

    /**
     * 设置 配置内容
     *
     * @param content 配置内容
     */
    void setContent(List<ConfigContent> content);

    /**
     * 获取 最后一次修改日期
     *
     * @return java.util.Date 最后一次修改日期
     */
    Long getUpdateTime();

    /**
     * 设置 最后一次修改日期
     *
     * @param updateDate 最后一次修改日期
     */
    void setUpdateTime(Long updateDate);

    /**
     * 获取分类ID
     *
     * @return 分类ID
     */
    String getClassifiedId();

    /**
     * 设置分类ID
     *
     * @param classifiedId 分类ID
     */
    void setClassifiedId(String classifiedId);

    ConfigEntity addContent(String key, Object value, String comment);

    ConfigContent get(String key);
}
