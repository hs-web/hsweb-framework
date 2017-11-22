
package org.hswebframework.web.authorization.listener;


import org.hswebframework.web.authorization.listener.event.AuthorizationEvent;

/**
 * 授权监听器,用于监听授权过程,以及自定义授权逻辑
 * 已弃用,请使用{@link org.springframework.context.ApplicationListener}
 *
 * @author zhouhao
 * @see AuthorizationEvent
 * @since 3.0
 */
@Deprecated
public interface AuthorizationListener<E extends AuthorizationEvent> {
    void on(E event);
}
