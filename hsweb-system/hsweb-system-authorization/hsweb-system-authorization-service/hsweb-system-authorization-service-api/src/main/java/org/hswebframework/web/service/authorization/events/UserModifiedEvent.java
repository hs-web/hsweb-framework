package org.hswebframework.web.service.authorization.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.entity.authorization.UserEntity;

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
public class UserModifiedEvent {
    private UserEntity userEntity;

    private boolean passwordModified;

    private boolean roleModified;
}
