package org.hswebframework.web.commons.bean;

/**
 * 支持验证的bean
 *
 * @author zhouhao
 * @since 3.0
 */
public interface ValidateBean extends Bean {

    /**
     * 尝试验证此bean,如果验证未通过,将抛出{@link org.hswebframework.web.validate.ValidationException}
     *
     * @param group 验证分组
     * @param <T>   当前对象类型
     * @return 当前对象
     */
    default <T extends ValidateBean> T tryValidate(Class... group) {
        BeanValidator.tryValidate(this, group);
        return (T) this;
    }
}
