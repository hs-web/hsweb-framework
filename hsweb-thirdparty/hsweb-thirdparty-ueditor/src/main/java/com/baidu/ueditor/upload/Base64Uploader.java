package com.baidu.ueditor.upload;

import com.baidu.ueditor.Context;
import com.baidu.ueditor.define.AppInfo;
import com.baidu.ueditor.define.BaseState;
import com.baidu.ueditor.define.FileType;
import com.baidu.ueditor.define.State;
import org.apache.commons.codec.binary.Base64;
import org.hswebframework.web.service.file.FileService;

import java.io.ByteArrayInputStream;
import java.util.Map;

public final class Base64Uploader {

    public static State save(String content, Map<String, Object> conf) {

        byte[] data = decode(content);

        long maxSize = (Long) conf.get("maxSize");

        if (!validSize(data, maxSize)) {
            return new BaseState(false, AppInfo.MAX_SIZE);
        }
        String fileUrlPrefix = (String) conf.get("rootPath");
        String suffix = FileType.getSuffix("JPG");
        try {
            FileService fileService = Context.FILE_SERVICE;
            String path = fileService.saveStaticFile(new ByteArrayInputStream(data), System.currentTimeMillis() + suffix);
            State state = new BaseState(true);
            state.putInfo("size", data.length);
            state.putInfo("title", "");
            state.putInfo("url", path);
            state.putInfo("type", suffix);
            return state;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BaseState(false, AppInfo.IO_ERROR);
    }

    private static byte[] decode(String content) {
        return Base64.decodeBase64(content);
    }

    private static boolean validSize(byte[] data, long length) {
        return data.length <= length;
    }

}