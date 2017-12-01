package com.baidu.ueditor.define;

import com.baidu.ueditor.Encoder;

import java.util.*;

/**
 * 多状态集合状态
 * 其包含了多个状态的集合, 其本身自己也是一个状态
 *
 * @author hancong03@baidu.com , zh.sqy@qq.com
 */
public class MultiState implements State {

    private boolean state = false;
    private String info = null;
    private Map<String, Long> intMap = new HashMap<>();
    private Map<String, String> infoMap = new HashMap<>();
    private List<String> stateList = new ArrayList<>();

    public MultiState(boolean state) {
        this.state = state;
    }

    public MultiState(boolean state, String info) {
        this.state = state;
        this.info = info;
    }

    public MultiState(boolean state, int infoKey) {
        this.state = state;
        this.info = AppInfo.getStateInfo(infoKey);
    }

    public boolean isSuccess() {
        return this.state;
    }

    public void addState(State state) {
        stateList.add(state.toJSONString());
    }

    /**
     * 该方法调用无效果
     */
    public void putInfo(String name, String val) {
        this.infoMap.put(name, val);
    }

    public String toJSONString() {

        String stateVal = this.isSuccess() ? AppInfo.getStateInfo(AppInfo.SUCCESS) : this.info;

        StringBuilder builder = new StringBuilder();

        builder.append("{\"state\": \"").append(stateVal).append("\"");

        //int
        this.intMap.forEach((k, v) -> builder.append(",\"").append(k).append("\": ").append(v));
        //string
        this.infoMap.forEach((k, v) -> builder.append(",\"").append(k).append("\": \"").append(v).append("\""));

        //list
        builder.append(", list: [").append(String.join(",", this.stateList.toArray(new String[this.stateList.size()])));

        builder.append(" ]}");

        return Encoder.toUnicode(builder.toString());

    }

    public void putInfo(String name, long val) {
        this.intMap.put(name, val);
    }

}
