package org.hswebframework.web.authorization.twofactor.defaults;

import org.hswebframework.web.authorization.twofactor.TwoFactorToken;
import org.hswebframework.web.authorization.twofactor.TwoFactorTokenManager;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhao
 * @since 3.0.4
 */
public class HashMapTwoFactorTokenManager implements TwoFactorTokenManager {

    private Map<String, WeakReference<TwoFactorTokenInfo>> tokens = new ConcurrentHashMap<>();

    private class TwoFactorTokenInfo implements Serializable {
        private static final long serialVersionUID = -5246224779564760241L;
        private volatile long lastRequestTime = System.currentTimeMillis();

        private long timeOut;

        private boolean isExpire() {
            return System.currentTimeMillis() - lastRequestTime >= timeOut;
        }
    }


    private String createTokenInfoKey(String userId, String operation) {
        return userId + "_" + operation;
    }

    private TwoFactorTokenInfo getTokenInfo(String userId, String operation) {
        return Optional.ofNullable(tokens.get(createTokenInfoKey(userId, operation)))
                .map(WeakReference::get)
                .orElse(null);
    }

    @Override
    public TwoFactorToken getToken(String userId, String operation) {

        return new TwoFactorToken() {
            private static final long serialVersionUID = -5148037320548431456L;

            @Override
            public void generate(long timeout) {
                TwoFactorTokenInfo info = new TwoFactorTokenInfo();
                info.timeOut = timeout;
                tokens.put(createTokenInfoKey(userId, operation), new WeakReference<>(info));
            }

            @Override
            public boolean expired() {
                TwoFactorTokenInfo info = getTokenInfo(userId, operation);
                if (info == null) {
                    return true;
                }
                if (info.isExpire()) {
                    tokens.remove(createTokenInfoKey(userId, operation));
                    return true;
                }
                info.lastRequestTime = System.currentTimeMillis();
                return false;
            }
        };
    }
}
