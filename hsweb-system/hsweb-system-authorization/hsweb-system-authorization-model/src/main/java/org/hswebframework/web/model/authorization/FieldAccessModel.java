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

package org.hswebframework.web.model.authorization;

import io.swagger.annotations.*;
import org.hswebframework.web.commons.model.Model;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author zhouhao
 */
@ApiModel(value = "FieldAccessModel", description = "字段级权限控制配置")
public class FieldAccessModel implements Model {
    private String field;

    private String describe;

    private List<ActionModel> actions;

    @ApiModelProperty(value = "要控制的字段名", required = true, example = "createTime")
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @ApiModelProperty("字段说明")
    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    @ApiModelProperty(value = "触发控制的事件,表示此字段不能进行{action}操作.",
//            example = "[{\"action\":\"query\"}]",
            required = true, dataType = "ActionModel")
    public List<ActionModel> getActions() {
        if (actions == null) actions = Collections.emptyList();
        return actions;
    }

    public void setActions(List<ActionModel> actions) {
        this.actions = actions;
    }

}
