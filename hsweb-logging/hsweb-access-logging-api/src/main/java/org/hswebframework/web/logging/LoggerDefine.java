package org.hswebframework.web.logging;


public class LoggerDefine {
    private String action;

    private String describe;

    public LoggerDefine(String action,String describe){
        this.action=action;
        this.describe=describe;
    }

    public String getDescribe() {
        return describe;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }
}

