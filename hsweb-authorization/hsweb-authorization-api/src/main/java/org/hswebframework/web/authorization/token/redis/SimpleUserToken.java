package org.hswebframework.web.authorization.token.redis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hswebframework.web.authorization.Authentication;
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
        Object authentication = map.get("authentication");
        if (authentication instanceof Authentication) {
            return FastBeanCopier.copy(map, new SimpleAuthenticationUserToken(((Authentication) authentication)));
        }
        return FastBeanCopier.copy(map, new SimpleUserToken());
    }

    public TokenState getState() {
        if (state == TokenState.normal) {
            checkExpired();
        }
        return state;
    }

    @Override
    public boolean checkExpired() {
        if (UserToken.super.checkExpired()) {
            setState(TokenState.expired);
            return true;
        }
        return false;
    }
}
