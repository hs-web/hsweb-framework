package org.hswebframework.web.service.authorization.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.entity.authorization.UserEntity;

/**
 * @author zhouhao
 * @since 3.0.4
 */
@Getter
@AllArgsConstructor
public class UserCreatedEvent {
    UserEntity userEntity;
}
