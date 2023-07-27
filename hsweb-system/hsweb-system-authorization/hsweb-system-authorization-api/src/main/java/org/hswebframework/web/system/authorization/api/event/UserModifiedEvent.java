package org.hswebframework.web.system.authorization.api.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.event.DefaultAsyncEvent;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;

/**
 * 用户修改事件,修改用户时将触发此事件.
 *
 * @author zhouhao
 * @see org.springframework.context.event.EventListener
 * @see org.springframework.context.ApplicationEventPublisher
 * @since 3.0
 */
@AllArgsConstructor
@Getter
public class UserModifiedEvent extends DefaultAsyncEvent {
    //修改前信息
    private UserEntity before;

    //修改后信息
    private UserEntity userEntity;

    //用户是否修改了密码
    private boolean passwordModified;
}
