package org.hswebframework.web.system.authorization.api.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.event.DefaultAsyncEvent;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;

/**
 * 用户密码发生修改时事件
 *
 * @author zhouhao
 * @see org.springframework.context.event.EventListener
 * @see org.springframework.context.ApplicationEventPublisher
 * @since 3.0
 */
@AllArgsConstructor
@Getter
public class UserModifiedEvent extends DefaultAsyncEvent {
    private UserEntity userEntity;

    private boolean passwordModified;
}
