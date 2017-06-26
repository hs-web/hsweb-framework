
package org.hswebframework.web.authorization.listener;


import org.hswebframework.web.authorization.listener.event.AuthorizationEvent;

/**
 * 授权监听器,用于监听授权过程,以及自定义授权逻辑
 *
 * @author zhouhao
 * @see AuthorizationEvent
 * @since 3.0
 */
public interface AuthorizationListener<E extends AuthorizationEvent> {
    void on(E event);
}
