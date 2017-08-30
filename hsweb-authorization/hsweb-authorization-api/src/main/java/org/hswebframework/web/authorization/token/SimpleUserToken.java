package org.hswebframework.web.authorization.token;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户令牌信息
 *
 * @author zhouhao
 * @since 3.0
 */
public class SimpleUserToken implements UserToken {

    private String userId;

    private String token;

    private volatile TokenState state;

    private AtomicLong requestTimesCounter = new AtomicLong(0);

    private volatile long lastRequestTime = System.currentTimeMillis();

    private volatile long firstRequestTime = System.currentTimeMillis();

    private volatile long requestTimes;

    private long maxInactiveInterval;

    @Override
    public long getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public void setMaxInactiveInterval(long maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    public SimpleUserToken(String userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public SimpleUserToken() {
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public long getRequestTimes() {
        return requestTimesCounter.get();
    }

    @Override
    public long getLastRequestTime() {
        return lastRequestTime;
    }

    @Override
    public long getSignInTime() {
        return firstRequestTime;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public TokenState getState() {
        return state;
    }

    public void setState(TokenState state) {
        this.state = state;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setFirstRequestTime(long firstRequestTime) {
        this.firstRequestTime = firstRequestTime;
    }

    public void setLastRequestTime(long lastRequestTime) {
        this.lastRequestTime = lastRequestTime;
    }

    public void setRequestTimes(long requestTimes) {
        this.requestTimes = requestTimes;
        requestTimesCounter.set(requestTimes);
    }

    void touch() {
        requestTimesCounter.addAndGet(1);
        lastRequestTime = System.currentTimeMillis();
    }
}
