package org.hswebframework.web.dev.tools.reader;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.File;

/**
 * @author zhouhao
 * @since 3.0
 */
@Data
@ApiModel("文件信息")
public class FileInfo {

    @ApiModelProperty("文件名")
    private String name;

    @ApiModelProperty("文件长度")
    private Long length;

    @ApiModelProperty("父目录")
    private String parent;

    @ApiModelProperty("文件长度")
    private String absolutePath;

    @ApiModelProperty("是否为文件")
    private boolean file;

    @ApiModelProperty("是否为目录")
    private boolean dir;

    public static FileInfo from(File file) {
        FileInfo info = new FileInfo();
        info.setName(file.getName());
        info.setLength(file.length());
        info.setParent(file.getParent());
        info.setAbsolutePath(file.getAbsolutePath());
        info.setFile(file.isFile());
        info.setDir(file.isDirectory());
        return info;
    }
}
