package org.hswebframework.web.controller.dashboard.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author zhouhao
 * @since 3.0
 */
@Data
@ApiModel("用户自定义仪表盘配置")
public class UserDashBoardResponse {
    private String id;

    private String type;

    private String name;

    private String template;

//    private Object data;

}
