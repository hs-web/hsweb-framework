package org.hswebframework.web.dict;

import java.lang.annotation.*;

/**
 * 字典选项注解
 *
 * @author zhouhao
 * @see Dict
 * @since 3.0
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Item {

    /**
     * @return 选项文本, 如: 男
     */
    String text() default "";

    /**
     * @return 选项值, 如: 1
     */
    String value() default "";

    /**
     * @return 字典说明
     */
    String[] comments() default {};
}
