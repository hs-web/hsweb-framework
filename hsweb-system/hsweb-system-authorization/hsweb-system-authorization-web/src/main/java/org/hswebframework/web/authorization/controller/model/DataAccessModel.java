/*
 *  Copyright 2019 http://www.hswebframework.org
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

package org.hswebframework.web.authorization.controller.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@ApiModel(value = "DataAccessModel",description = "数据级权限控制配置")
public class DataAccessModel {
    private String action;

    private String describe;

    private String type;

    private String config;

    @ApiModelProperty(value = "触发控制的事件", example = "query", required = true)
    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @ApiModelProperty("说明")
    public String getDescribe() {
        return this.describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    @ApiModelProperty(value = "控制的类型", allowableValues = "OWN_CREATED,SCRIPT,CUSTOM", required = true, example = "OWN_CREATED")
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @ApiModelProperty(value = "控制配置." +
            "当控制类型为OWN_CREATED时:可留空." +
            "当控制类型为SCRIPT时:值为json string,格式:{\"language\":\"groovy\",\"script\":\"return true;\"}." +
            "当控制类型为CUSTOM时,值为一个实现了DataAccessController接口的类")
    public String getConfig() {
        return this.config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

}
