package org.hswebframework.web.authorization.access;

import org.hswebframework.web.authorization.define.AuthorizingContext;

/**
 * 数据级别权限控制器,通过此控制器对当前登录用户进行的操作进行数据级别的权限控制。
 * 如：A用户只能查询自己创建的B数据,A用户只能修改自己创建的B数据
 *
 * @author zhouhao
 * @since  3.0
 */
public interface DataAccessController {
    /**
     * 执行权限控制
     * @param access 控制方式以及配置
     * @param context 权限验证上下文，用于传递验证过程用到的参数
     * @return 授权是否通过
     */
    boolean doAccess(DataAccessConfig access, AuthorizingContext context);
}
