package org.hsweb.web.socket.cmd.support;

import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.socket.cmd.CMD;
import org.hsweb.web.socket.message.WebSocketMessage;
import org.hsweb.web.socket.message.WebSocketMessageManager;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by zhouhao on 16-5-29.
 */
public class SystemMonitorProcessor extends AbstractCmdProcessor {


    private Sigar sigar;
    private ExecutorService exec = Executors.newCachedThreadPool();
    private Map<String, Publish> cpuPublish = new ConcurrentHashMap<>();
    private Map<String, Publish> memPublish = new ConcurrentHashMap<>();

    private boolean cpuMonitorIsStarted, memMonitorIsStarted;

    public SystemMonitorProcessor() {
        sigar = new Sigar();
    }

    public void setWebSocketMessageManager(WebSocketMessageManager webSocketMessageManager) {
        this.webSocketMessageManager = webSocketMessageManager;
    }

    @Override
    public String getName() {
        return "system-monitor";
    }

    @Override
    public void exec(CMD cmd) throws Exception {
        String type = ((String) cmd.getParams().get("type"));
        if (type == null) return;
        String userId = getUser(cmd).getId();
        switch (type) {
            case "cpu":
                Publish publish = cpuPublish.get(userId);
                if (publish == null) {
                    publish = new Publish();
                    publish.setUserId(userId);
                    publish.setCallback((String) cmd.getParams().get("callback"));
                    cpuPublish.put(userId, publish);
                }
                publish.addSession(cmd.getSession());
                if (!cpuMonitorIsStarted) {
                    startPublishCpu();
                    cpuMonitorIsStarted = true;
                }
                webSocketMessageManager.subscribe(getName(), userId, cmd.getSession());
                break;
            case "mem":
                publish = memPublish.get(userId);
                if (publish == null) {
                    publish = new Publish();
                    publish.setUserId(userId);
                    publish.setCallback((String) cmd.getParams().get("callback"));
                    memPublish.put(userId, publish);
                }
                publish.addSession(cmd.getSession());
                if (!memMonitorIsStarted) {
                    startPublishMem();
                    memMonitorIsStarted = true;
                }
                webSocketMessageManager.subscribe(getName(), userId, cmd.getSession());
                break;
            case "mem-cancel":
                cancelPublish(memPublish, userId, cmd.getSession());
                break;
            case "cpu-cancel":
                cancelPublish(cpuPublish, userId, cmd.getSession());
                break;
            case "cancel":
                cancelPublish(memPublish, userId, cmd.getSession());
                cancelPublish(cpuPublish, userId, cmd.getSession());
                webSocketMessageManager.deSubscribe(getName(), userId, cmd.getSession());
                break;
        }
    }

    protected void cancelPublish(Map<String, Publish> publishMap, String userId, WebSocketSession socketSession) {
        Publish publish = publishMap.get(userId);
        if (publish != null) {
            publish.removeSession(socketSession);
            if (publish.getSessionMap().isEmpty())
                publishMap.remove(userId);
        }
    }

    public User getUser(CMD cmd) {
        return getUser(cmd.getSession());
    }

    class Publish {
        private String userId;
        private String callback;
        private Map<String, WebSocketSession> sessionMap = Collections.synchronizedMap(new HashMap<>());

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getCallback() {
            return callback;
        }

        public void setCallback(String callback) {
            this.callback = callback;
        }

        public Map<String, WebSocketSession> getSessionMap() {
            return sessionMap;
        }

        public void removeSession(WebSocketSession socketSession) {
            sessionMap.remove(socketSession.getId());
        }

        public void addSession(WebSocketSession socketSession) {
            sessionMap.put(socketSession.getId(), socketSession);
        }
    }

    public Future startPublishCpu() throws Exception {
        return exec.submit((Callable) () -> {
            for (; ; ) {
                try {
                    if (cpuPublish.isEmpty()) {
                        Thread.sleep(1000);
                        if (cpuPublish.isEmpty()) {
                            cpuMonitorIsStarted = false;
                            return null;
                        }
                    }
                    List<Map> infoList = new LinkedList<>();
                    CpuInfo[] cpuInfo = sigar.getCpuInfoList();
                    CpuPerc[] cpuPercs = sigar.getCpuPercList();
                    for (int i = 0; i < cpuInfo.length; i++) {
                        Map info = cpuInfo[i].toMap();
                        info.put("perc", cpuPercs[i]);
                        infoList.add(info);
                    }
                    cpuPublish.values().forEach(publish -> {
                        WebSocketMessage msg = new WebSocketMessage();
                        msg.setTo(publish.getUserId());
                        msg.setContent(infoList);
                        msg.setType(getName());
                        msg.setCallBack(publish.getCallback());
                        msg.setFrom("system");
                        try {
                            webSocketMessageManager.publish(msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    });
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
        });
    }

    public Future startPublishMem() throws Exception {
        return exec.submit((Callable) () -> {
            for (; ; ) {
                try {
                    if (memPublish.isEmpty()) {
                        Thread.sleep(1000);
                        if (memPublish.isEmpty()) {
                            memMonitorIsStarted = false;
                            return null;
                        }
                    }
                    Map<String, Object> map = sigar.getMem().toMap();
                    Runtime runtime=  Runtime.getRuntime();
                    map.put("jvmTotal", runtime.totalMemory());
                    map.put("jvmMax",runtime.maxMemory());
                    map.put("jvmFree",runtime.freeMemory());
                    memPublish.values().forEach(publish -> {
                        WebSocketMessage msg = new WebSocketMessage();
                        msg.setTo(publish.getUserId());
                        msg.setContent(map);
                        msg.setType(getName());
                        msg.setCallBack(publish.getCallback());
                        msg.setFrom("system");
                        try {
                            webSocketMessageManager.publish(msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    });
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
        });
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public void onSessionConnect(WebSocketSession session) throws Exception {
    }

    @Override
    public void onSessionClose(WebSocketSession session) throws Exception {
        User user = getUser(session);
        if (user != null) {
            cancelPublish(cpuPublish, user.getId(), session);
            cancelPublish(memPublish, user.getId(), session);
            webSocketMessageManager.deSubscribe(getName(), user.getId(),session);
        }

    }

}
