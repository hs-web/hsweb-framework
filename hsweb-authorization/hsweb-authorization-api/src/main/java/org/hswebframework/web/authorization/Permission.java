/*
 * Copyright 2019 http://www.hswebframework.org
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

import lombok.NonNull;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.access.FieldFilterDataAccessConfig;
import org.hswebframework.web.authorization.access.ScopeDataAccessConfig;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.hswebframework.web.authorization.access.DataAccessConfig.DefaultType.DENY_FIELDS;

/**
 * 用户持有的权限信息,包含了权限基本信息、可操作范围(action)、行,列级权限控制规则。
 * 是用户权限的重要接口。
 *
 * @author zhouhao
 * @see Authentication
 * @since 3.0
 */
public interface Permission extends Serializable {
    /**
     * 查询
     */
    String ACTION_QUERY = "query";
    /**
     * 获取明细
     */
    String ACTION_GET = "get";
    /**
     * 新增
     */
    String ACTION_ADD = "add";
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
     * @return 权限ID，权限的唯一标识
     */
    String getId();

    /**
     * 用户对此权限的可操作事件(按钮)
     * <p>
     * ⚠️:任何时候都不应该对返回的Set进行写操作
     *
     * @return 如果没有配置返回空{@link Collections#emptySet()},不会返回null.
     */
    Set<String> getActions();

    /**
     * 用户对此权限持有的数据权限信息, 用于数据级别的控制
     * <p>
     * ⚠️:任何时候都不应该对返回的Set进行写操作
     *
     * @return 如果没有配置返回空{@link Collections#emptySet()},不会返回null.
     * @see DataAccessConfig
     * @see org.hswebframework.web.authorization.access.DataAccessController
     */
    Set<DataAccessConfig> getDataAccesses();


    /**
     * 查找数据权限配置
     *
     * @param configPredicate 数据权限配置匹配规则
     * @param <T>             数据权限配置类型
     * @return {@link Optional}
     * @see this#scope(String, String, String)
     */
    @SuppressWarnings("all")
    default <T extends DataAccessConfig> Optional<T> findDataAccess(DataAccessPredicate<T> configPredicate) {
        return (Optional) getDataAccesses().stream()
                .filter(configPredicate)
                .findFirst();
    }

    /**
     * 查找字段过滤的数据权限配置(列级数据权限),比如:不查询某些字段
     *
     * @param action 权限操作类型 {@link Permission#ACTION_QUERY}
     * @return {@link Optional}
     * @see FieldFilterDataAccessConfig
     * @see FieldFilterDataAccessConfig#getFields()
     */
    default Optional<FieldFilterDataAccessConfig> findFieldFilter(String action) {
        return findDataAccess(conf -> FieldFilterDataAccessConfig.class.isInstance(conf) && conf.getAction().equals(action));
    }


    /**
     * 获取不能执行操作的字段
     *
     * @param action 权限操作
     * @return 未配置时返回空set, 不会返回null
     */
    default Set<String> findDenyFields(String action) {
        return findFieldFilter(action)
                .filter(conf -> DENY_FIELDS.equals(conf.getType()))
                .map(FieldFilterDataAccessConfig::getFields)
                .orElseGet(Collections::emptySet);
    }


    /**
     * 查找数据范围权限控制配置(行级数据权限),比如: 只能查询本机构的数据
     *
     * @param type      范围类型标识,由具体的实现定义,如: 机构范围
     * @param scopeType 范围类型,由具体的实现定义,如: 只能查看自己所在机构
     * @param action    权限操作 {@link Permission#ACTION_QUERY}
     * @return 未配置时返回空set, 不会返回null
     */
    default Set<Object> findScope(String action, String type, String scopeType) {
        return findScope(scope(action, type, scopeType));
    }

    default Set<Object> findScope(Permission.DataAccessPredicate<ScopeDataAccessConfig> predicate) {
        return findDataAccess(predicate)
                .map(ScopeDataAccessConfig::getScope)
                .orElseGet(Collections::emptySet);
    }

    /**
     * 构造一个数据范围权限控制配置查找逻辑
     *
     * @param type      范围类型标识,由具体的实现定义,如: 机构范围
     * @param scopeType 范围类型,由具体的实现定义,如: 只能查看自己所在机构
     * @param action    权限操作 {@link Permission#ACTION_QUERY}
     * @return {@link DataAccessPredicate}
     */
    static Permission.DataAccessPredicate<ScopeDataAccessConfig> scope(String action, String type, String scopeType) {
        Objects.requireNonNull(action, "action can not be null");
        Objects.requireNonNull(type, "type can not be null");
        Objects.requireNonNull(scopeType, "scopeType can not be null");

        return config ->
                config instanceof ScopeDataAccessConfig
                        && action.equals(config.getAction())
                        && type.equals(config.getType())
                        && scopeType.equals(((ScopeDataAccessConfig) config).getScopeType());
    }


    /**
     * 数据权限查找判断逻辑接口
     *
     * @param <T>
     */
    interface DataAccessPredicate<T extends DataAccessConfig> extends Predicate<DataAccessConfig> {
        boolean test(DataAccessConfig config);


        @Override
        default DataAccessPredicate<T> and(Predicate<? super DataAccessConfig> other) {
            return (t) -> test(t) && other.test(t);
        }

        @Override
        default DataAccessPredicate<T> or(Predicate<? super DataAccessConfig> other) {
            return (t) -> test(t) || other.test(t);
        }
    }

}
