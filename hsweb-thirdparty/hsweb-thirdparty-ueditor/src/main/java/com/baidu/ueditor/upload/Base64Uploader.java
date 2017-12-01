package com.baidu.ueditor.upload;

import com.baidu.ueditor.Context;
import com.baidu.ueditor.define.AppInfo;
import com.baidu.ueditor.define.BaseState;
import com.baidu.ueditor.define.FileType;
import com.baidu.ueditor.define.State;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.hswebframework.web.service.file.FileService;

import java.io.ByteArrayInputStream;
import java.util.Map;

@Slf4j
public final class Base64Uploader {

    public static State save(String content, Map<String, Object> conf) {

        byte[] data = decode(content);

        long maxSize = (Long) conf.get("maxSize");

        if (!validSize(data, maxSize)) {
            return new BaseState(false, AppInfo.MAX_SIZE);
        }
        String suffix = FileType.getSuffix(FileType.JPG);
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
            log.error("上传base64文件失败",e);
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