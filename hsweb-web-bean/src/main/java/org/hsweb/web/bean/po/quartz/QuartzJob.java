package org.hsweb.web.bean.po.quartz;

import org.hsweb.web.bean.po.GenericPo;

public class QuartzJob extends GenericPo<String> {
    private String name;

    private String remark;

    private String cron;

    private String script;

    private String language = "groovy";

    private boolean running;

    private boolean enabled;

    private boolean ready;

    private long lastRunningStartTime;

    private long lastRunningEndTime;

    private String lastResult;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getLastRunningStartTime() {
        return lastRunningStartTime;
    }

    public void setLastRunningStartTime(long lastRunningStartTime) {
        this.lastRunningStartTime = lastRunningStartTime;
    }

    public long getLastRunningEndTime() {
        return lastRunningEndTime;
    }

    public void setLastRunningEndTime(long lastRunningEndTime) {
        this.lastRunningEndTime = lastRunningEndTime;
    }

    public String getLastResult() {
        return lastResult;
    }

    public void setLastResult(String lastResult) {
        this.lastResult = lastResult;
    }
}
