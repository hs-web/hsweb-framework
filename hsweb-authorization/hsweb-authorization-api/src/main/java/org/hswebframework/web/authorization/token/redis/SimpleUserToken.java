package org.hswebframework.web.authorization.token.redis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hswebframework.web.authorization.token.TokenState;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.bean.FastBeanCopier;

import java.util.Map;

@Getter
@Setter
@ToString(exclude = "token")
@EqualsAndHashCode(of = "token")
public class SimpleUserToken implements UserToken {

    private String userId;

    private String token;

    private long requestTimes;

    private long lastRequestTime;

    private long signInTime;

    private TokenState state;

    private String type;

    private long maxInactiveInterval;

    public static SimpleUserToken of(Map<String, Object> map) {

        return FastBeanCopier.copy(map, new SimpleUserToken());
    }

    @Override
    public boolean isNormal() {
        if (checkExpired()) {
            setState(TokenState.expired);
            return false;
        }
        return UserToken.super.isNormal();
    }
}
