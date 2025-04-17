package org.hswebframework.web.authorization.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.event.DefaultAsyncEvent;

@Getter
@Setter
@AllArgsConstructor
public class UserTokenBeforeCreateEvent extends DefaultAsyncEvent {
    private final UserToken token;

    /**
     * 过期时间，单位毫秒，-1为不过期.
     */
    private long expires;

}
