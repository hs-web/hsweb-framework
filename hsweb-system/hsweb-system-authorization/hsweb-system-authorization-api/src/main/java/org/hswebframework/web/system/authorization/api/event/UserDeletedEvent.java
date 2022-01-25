package org.hswebframework.web.system.authorization.api.event;

import lombok.Getter;
import org.hswebframework.web.authorization.DefaultDimensionType;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;

@Getter
public class UserDeletedEvent extends DimensionDeletedEvent {

    private final UserEntity user;

    public UserDeletedEvent(UserEntity user) {
        super(DefaultDimensionType.user.getId(), user.getId());
        this.user = user;
    }
}
