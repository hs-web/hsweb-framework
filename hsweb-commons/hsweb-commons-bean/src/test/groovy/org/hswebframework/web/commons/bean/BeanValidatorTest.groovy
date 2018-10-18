package org.hswebframework.web.commons.bean

import org.hibernate.validator.constraints.NotBlank
import org.hswebframework.web.validator.group.CreateGroup
import org.hswebframework.web.validator.group.UpdateGroup

/**
 * @author zhouhao
 * @since 3.0.2
 */
class BeanValidatorTest extends spock.lang.Specification {

    def "测试初始化验证器"() {
        given: "初始化"
        def validator = BeanValidator.getValidator();
        expect: "成功"
        null != validator
    }


    def doValidate(TestBean bean, Class group) {
        try {
            bean.tryValidate(group);
            return null;
        } catch (Exception e) {
            return e.message;
        }
    }

    def "测试group验证"() {
        expect: "验证多个group"
        doValidate(new TestBean(name: name), group as Class) == message
        where:
        name | group             | message
        null | CreateGroup.class | "姓名不能为空"
        ""   | CreateGroup.class | "姓名不能为空"
        null | UpdateGroup.class | null
        ""   | UpdateGroup.class | "长度必须在2-20之间"
        "张三" | UpdateGroup.class | null
    }


}
