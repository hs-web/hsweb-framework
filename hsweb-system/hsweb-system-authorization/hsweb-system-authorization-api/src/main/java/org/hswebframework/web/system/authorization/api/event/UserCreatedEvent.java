package org.hswebframework.web.system.authorization.api.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.event.DefaultAsyncEvent;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;

/**
 * @author zhouhao
 * @since 3.0.4
 */
@Getter
@AllArgsConstructor
public class UserCreatedEvent extends DefaultAsyncEvent {
    UserEntity userEntity;
}
