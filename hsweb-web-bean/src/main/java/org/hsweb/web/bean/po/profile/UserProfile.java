package org.hsweb.web.bean.po.profile;

import org.hibernate.validator.constraints.NotBlank;
import org.hsweb.web.bean.po.GenericPo;

/**
 * 用户配置文件
 * Created by zhouhao on 16-7-4.
 */
public class UserProfile extends GenericPo<String> {

    @NotBlank(message = "用户不能为空")
    private String userId;
    @NotBlank(message = "类型不能为空")
    private String type;
    @NotBlank(message = "内容不能为空")
    private String content;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
