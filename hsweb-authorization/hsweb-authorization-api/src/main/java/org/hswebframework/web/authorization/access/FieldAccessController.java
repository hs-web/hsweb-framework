package org.hswebframework.web.authorization.access;

import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.boost.aop.context.MethodInterceptorParamContext;

import java.util.Set;

/**
 * 字段级权限控制器,用于控制对字段的操作权限。如:不同角色,可操作的字段不同等
 *
 * @author zhouhao
 */
public interface FieldAccessController {

    /**
     * 执行权限验证。根据当前被拦截的操作类型,以及此类型可操作的字段集合进行权限验证
     *
     * @param action   当前操作的类型 {@link Permission#getActions()}
     * @param accesses 不可操作的字段
     * @param params   参数上下文
     * @return 验证是否通过
     */
    boolean doAccess(String action, Set<FieldAccessConfig> accesses, MethodInterceptorParamContext params);
}
