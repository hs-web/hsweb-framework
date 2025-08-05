package org.hswebframework.web.authorization.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标记多个维度的权限控制相关配置
 *
 * @author zhouhao
 * @since 5.0.1
 */
@Target({ElementType.METHOD, TYPE, ANNOTATION_TYPE, FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Dimensions {

    /**
     * 存在多个维度时的判断逻辑,默认任意一个满足则认为有权限
     *
     * @return Logical
     */
    Logical logical() default Logical.DEFAULT;

    /**
     * @return 针对当前配置的说明信息
     */
    String[] description() default {};

}