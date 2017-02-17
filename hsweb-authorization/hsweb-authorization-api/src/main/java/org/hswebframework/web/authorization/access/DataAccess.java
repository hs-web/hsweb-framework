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

package org.hswebframework.web.authorization.access;


import org.hswebframework.web.authorization.Permission;

import java.io.Serializable;

/**
 * 数据级的权限控制,此接口为控制方式配置
 * 具体的控制逻辑由控制器{@link DataAccessController}实现
 *
 * @author zhouhao
 * @see org.hswebframework.web.authorization.access.CustomDataAccess
 * @see org.hswebframework.web.authorization.access.OwnCreatedDataAccess
 * @see org.hswebframework.web.authorization.access.ScriptDataAccess
 */
public interface DataAccess extends Serializable {

    /**
     * 对数据的操作事件
     *
     * @return 操作时间
     * @see Permission#ACTION_ADD
     * @see Permission#ACTION_DELETE
     * @see Permission#ACTION_GET
     * @see Permission#ACTION_QUERY
     * @see Permission#ACTION_UPDATE
     */
    String getAction();

    /**
     * 控制方式标识
     *
     * @return 控制方式
     * @see Type#name()
     */
    String getType();

    /**
     * 内置3中控制方式
     */
    enum Type {
        OWN_CREATED("自己创建的数据"),
        SCRIPT("脚本"),
        CUSTOM("自定义控制器");

        public final String text;

        Type(String text) {
            this.text = text;
        }
    }
}
