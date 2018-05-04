package org.hswebframework.web.dev.tools.writer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author zhouhao
 * @since 3.0
 */
@Data
@ApiModel("文件信息")
public class GeneratedCode {

    @ApiModelProperty("文件名")
    private String file;

    @ApiModelProperty("文件类型,file:文件,dir:目录")
    private String type;

    @ApiModelProperty("相同文件替换方式,ignore:跳过,append:追加,其他覆盖")
    private String repeat;

    @ApiModelProperty("文件内容")
    private String template;

    @ApiModelProperty("子文件")
    private List<GeneratedCode> children;
}
