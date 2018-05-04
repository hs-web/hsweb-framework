package org.hswebframework.web.dev.tools.reader;

import lombok.Data;

import java.io.File;

/**
 * @author zhouhao
 * @since 3.0
 */
@Data
public class FileInfo {
    private String name;

    private Long length;

    private String parent;

    private String absolutePath;

    private String type;

    private boolean file;

    private boolean dir;

    public static FileInfo from(File file){
        FileInfo info=new FileInfo();
        info.setName(file.getName());
        info.setLength(file.length());
        info.setParent(file.getParent());
        info.setAbsolutePath(file.getAbsolutePath());
        info.setFile(file.isFile());
        info.setDir(file.isDirectory());
        return  info;
    }
}
