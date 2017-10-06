package org.hswebframework.web.authorization.token;


import org.hswebframework.web.authorization.User;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;

import java.io.Serializable;

/**
 * 用户的token信息
 *
 * @author zhouhao
 * @since 3.0
 */
public interface UserToken extends Serializable, Comparable<UserToken> {
    /**
     * @return 用户id
     * @see User#getId()
     */
    String getUserId();

    /**
     * @return token
     */
    String getToken();

    /**
     * @return 请求总次数
     */
    long getRequestTimes();

    /**
     * @return 最后一次请求时间
     */
    long getLastRequestTime();

    /**
     * @return 首次请求时间
     */
    long getSignInTime();

    /**
     * @return 令牌状态
     */
    TokenState getState();


    long getMaxInactiveInterval();

    /**
     * @return 是否正常
     */
    default boolean isEffective() {
        return getState() == TokenState.effective;
    }

    /**
     * @return 是否已过期
     */
    default boolean isExpired() {
        return getState() == TokenState.expired;
    }

    /**
     * @return 是否离线
     */
    default boolean isOffline() {
        return getState() == TokenState.offline;
    }

    default boolean validate() {
        if (!isEffective()) {
            throw new UnAuthorizedException(getState());
        }
        return true;
    }

    @Override
    default int compareTo(UserToken target) {
        return Long.valueOf(getSignInTime()).compareTo(target.getSignInTime());
    }
}
