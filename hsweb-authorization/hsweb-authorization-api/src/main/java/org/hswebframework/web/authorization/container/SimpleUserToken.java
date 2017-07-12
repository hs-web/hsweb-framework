package org.hswebframework.web.authorization.container;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhouhao on 2017/7/7.
 */
public class SimpleUserToken implements UserToken {

    private String userId;

    private String token;

    private AtomicLong requestTimesCounter=new AtomicLong(0);

    private volatile long lastRequestTime=System.currentTimeMillis();

    private volatile long firstRequestTime=System.currentTimeMillis();

    private long requestTimes;

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

    public  synchronized void touch(){
        requestTimesCounter.addAndGet(1);
        lastRequestTime=System.currentTimeMillis();
    }
}
