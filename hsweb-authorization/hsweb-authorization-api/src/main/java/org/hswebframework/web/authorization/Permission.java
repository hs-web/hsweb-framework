/*
 * Copyright 2016 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web.authorization;

import org.hswebframework.web.authorization.access.DataAccess;
import org.hswebframework.web.authorization.access.FieldAccess;

import java.io.Serializable;
import java.util.Set;

/**
 * 用户持有的权限信息
 *
 * @author zhouhao
 * @see Authorization
 * @since 3.0
 */
public interface Permission extends Serializable {

    /**
     * 查询
     */
    String ACTION_QUERY  = "query";
    /**
     * 获取明细
     */
    String ACTION_GET    = "get";
    /**
     * 新增
     */
    String ACTION_ADD    = "add";
    /**
     * 更新
     */
    String ACTION_UPDATE = "update";
    /**
     * 删除
     */
    String ACTION_DELETE = "delete";
    /**
     * 导入
     */
    String ACTION_IMPORT = "import";
    /**
     * 导出
     */
    String ACTION_EXPORT = "export";

    /**
     * 禁用
     */
    String ACTION_DISABLE = "disable";

    /**
     * 启用
     */
    String ACTION_ENABLE = "enable";

    /**
     * 获取权限ID，权限的唯一标识
     *
     * @return id
     */
    String getId();

    /**
     * 获取用户对此权限的可操作事件(按钮)
     *
     * @return 操作事件(按钮)集合
     */
    Set<String> getActions();

    /**
     * 获取用户对此权限持有的字段权限信息,用于字段级别的控制
     *
     * @return 可操作字段集合
     * @see FieldAccess
     */
    Set<FieldAccess> getFieldAccesses();

    /**
     * 获取用户对此权限持有的数据权限信息,用于数据级别的控制
     *
     * @return 数据权限信息
     * @see DataAccess
     */
    Set<DataAccess> getDataAccesses();
}
