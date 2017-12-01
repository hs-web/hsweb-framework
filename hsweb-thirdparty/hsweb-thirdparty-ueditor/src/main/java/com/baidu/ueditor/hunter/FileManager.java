package com.baidu.ueditor.hunter;

import com.baidu.ueditor.define.MultiState;
import com.baidu.ueditor.define.State;

import java.io.File;
import java.util.Map;

public class FileManager {

    private String   dir        = null;
    private String   rootPath   = null;
    private String[] allowFiles = null;
    private int      count      = 0;

    public FileManager(Map<String, Object> conf) {

        this.rootPath = (String) conf.get("rootPath");
        this.dir = this.rootPath + (String) conf.get("dir");
        this.allowFiles = this.getAllowFiles(conf.get("allowFiles"));
        this.count = (Integer) conf.get("count");

    }

    public State listFile(int index) {

        State state = new MultiState(true);

        state.putInfo("start", index);
        state.putInfo("total",0);

        return state;

    }

//    private State getState(List<FileInfoEntity> resources) {
//
//        MultiState state = new MultiState(true);
//        BaseState fileState = null;
//
//        for (FileInfoEntity obj : resources) {
//            if (obj == null) {
//                break;
//            }
//            fileState = new BaseState(true);
//            fileState.putInfo("url", rootPath + "file/download/" + obj.getId() + "/" + obj.getName());
//            state.addState(fileState);
//        }
//
//        return state;
//
//    }

    private String getPath(File file) {

        String path = file.getAbsolutePath();
        path = path.replace("\\", "/");
        return path.replace(this.rootPath, "/");

    }

    private String[] getAllowFiles(Object fileExt) {

        String[] exts = null;
        String ext = null;

        if (fileExt == null) {
            return new String[0];
        }

        exts = (String[]) fileExt;

        for (int i = 0, len = exts.length; i < len; i++) {

            ext = exts[i];
            exts[i] = ext.replace(".", "");

        }

        return exts;

    }

    public static void main(String[] args) {
        int n = 13;
        System.out.println(n > 9 ? (char) (n - 10 + 'A') : n);
        System.out.println(n > 9 ? (char) (n - 10 + 'A') : 1);
    }
}
