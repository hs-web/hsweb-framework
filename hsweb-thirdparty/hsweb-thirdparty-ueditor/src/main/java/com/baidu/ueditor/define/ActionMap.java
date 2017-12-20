package com.baidu.ueditor.define;

import org.hswebframework.web.Maps;

import java.util.HashMap;
import java.util.Map;

/**
 * 定义请求action类型
 *
 * @author hancong03@baidu.com
 */
@SuppressWarnings("serial")
public final class ActionMap {

    public static final Map<String, Integer> mapping;
    // 获取配置请求
    public static final int CONFIG = 0;
    public static final int UPLOAD_IMAGE = 1;
    public static final int UPLOAD_SCRAWL = 2;
    public static final int UPLOAD_VIDEO = 3;
    public static final int UPLOAD_FILE = 4;
    public static final int CATCH_IMAGE = 5;
    public static final int LIST_FILE = 6;
    public static final int LIST_IMAGE = 7;

    static {
        mapping = Maps.<String, Integer>buildMap().
                put("config", ActionMap.CONFIG).
                put("uploadimage", ActionMap.UPLOAD_IMAGE).
                put("uploadscrawl", ActionMap.UPLOAD_SCRAWL).
                put("uploadvideo", ActionMap.UPLOAD_VIDEO).
                put("uploadfile", ActionMap.UPLOAD_FILE).
                put("catchimage", ActionMap.CATCH_IMAGE).
                put("listfile", ActionMap.LIST_FILE).
                put("listimage", ActionMap.LIST_IMAGE).get();
    }

    public static int getType(String key) {
        return ActionMap.mapping.get(key);
    }

}
