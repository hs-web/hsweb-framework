package org.hswebframework.web.workflow.flowable.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("工作流模型创建请求")
public class ModelCreateRequest {

    @ApiModelProperty(value = "模型标识",example = "test_model")
    private String key;

    @ApiModelProperty(value = "模型名称",example = "测试模型")
    private String name;

    @ApiModelProperty(value = "模型说明")
    private String description;
}
