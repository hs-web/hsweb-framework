package org.hswebframework.web.dict;


import java.lang.annotation.*;

/**
 * @author zhouhao
 * @since 3.0
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Dict {
    /**
     * @return 字典ID
     * @see DictDefine#getId()
     * @see DictDefineRepository
     */
    String value() default "";

    /**
     * 字典别名
     * @return 别名
     */
    String alias() default "";

    /**
     * @return 字典说明, 备注
     */
    String comments() default "";


}
