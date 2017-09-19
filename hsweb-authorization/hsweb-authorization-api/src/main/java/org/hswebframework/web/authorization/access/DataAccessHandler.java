package org.hswebframework.web.authorization.access;

import org.hswebframework.web.authorization.define.AuthorizingContext;

/**
 * 数据级别权限控制处理器接口,负责处理支持的权限控制配置
 *
 * @author zhouhao
 */
public interface DataAccessHandler {

    /**
     * 是否支持处理此配置
     *
     * @param access 控制配置
     * @return 是否支持
     */
    boolean isSupport(DataAccessConfig access);

    /**
     * 执行处理,返回处理结果
     *
     * @param access  控制配置
     * @param context 参数上下文
     * @return 处理结果
     */
    boolean handle(DataAccessConfig access, AuthorizingContext context);
}
