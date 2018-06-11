package org.hswebframework.web.service.authorization.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhouhao
 * @see org.springframework.context.event.EventListener
 * @since 3.0.0-RC
 */
@AllArgsConstructor
@Getter
public class ClearUserAuthorizationCacheEvent {
    private String userId;

    private boolean all;
}
