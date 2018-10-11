package org.hswebframework.web.commons.bean;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.validator.group.CreateGroup;
import org.hswebframework.web.validator.group.UpdateGroup;

/**
 * @author zhouhao
 * @since 3.0.2
 */
@Data
public class TestBean implements ValidateBean {

    @NotBlank(groups = CreateGroup.class, message = "姓名不能为空")
    @Length(min = 2, max = 20, message = "长度必须在2-20之间", groups = UpdateGroup.class)
    private String name;

}
