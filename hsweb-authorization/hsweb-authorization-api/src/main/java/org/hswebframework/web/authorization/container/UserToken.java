package org.hswebframework.web.authorization.container;


import java.io.Serializable;

/**
 * Created by zhouhao on 2017/7/7.
 */
public interface UserToken extends Serializable, Comparable<UserToken> {
    String getUserId();

    String getToken();

    long getRequestTimes();

    long getLastRequestTime();

    long getSignInTime();

    @Override
    default int compareTo(UserToken o) {
        return Long.valueOf(getSignInTime()).compareTo(o.getSignInTime());
    }
}
