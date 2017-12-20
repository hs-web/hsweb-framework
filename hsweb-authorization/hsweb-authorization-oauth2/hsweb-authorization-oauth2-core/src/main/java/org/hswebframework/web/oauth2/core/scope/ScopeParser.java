package org.hswebframework.web.oauth2.core.scope;

import org.hswebframework.web.authorization.Permission;

import java.util.Set;

/**
 * scope解析器
 *
 * @author zhouhao
 */
public interface ScopeParser {
    /**
     * 将文本解析为Set
     * <pre>
     *     user-info:get user-share:push
     * </pre>
     * <pre>
     *     Set{"user-info:get","user-share:push"}
     * </pre>
     *
     * @param scopeText socket文本
     * @return socpe集合
     */
    Set<String> fromScopeText(String scopeText);

    String toScopeText(Set<String> scopeText);

    /**
     * 将scope解析为Permission
     *
     * @param scope scope集合
     * @return permission集合。如果参数为null或者空，则返回空集合
     */
    Set<Permission> parsePermission(Set<String> scope);

    default Set<Permission> parsePermission(String scopeText) {
        return parsePermission(fromScopeText(scopeText));
    }
}
