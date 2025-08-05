package org.hswebframework.web.authorization.annotation;

import org.hswebframework.web.authorization.DimensionType;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 请使用注解继承方式使用此注解
 *
 * @author zhouhao
 * @see RequiresRoles
 * @since 4.0
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Repeatable(value = Dimension.List.class)
public @interface Dimension {

    /**
     * 维度类型标识,如: role,org
     *
     * @return 维度类型
     * @see org.hswebframework.web.authorization.Dimension#getType()
     * @see DimensionType#getId()
     * @see org.hswebframework.web.authorization.Authentication#hasDimension(String, String...)
     */
    String type();

    /**
     * 具体的维度ID,如: 角色ID,组织ID
     *
     * @return 维度ID
     * @see org.hswebframework.web.authorization.Dimension#getId()
     * @see org.hswebframework.web.authorization.Authentication#hasDimension(String, String...)
     */
    String[] id() default {};

    /**
     * 配置了多个ID时的判断逻辑,默认为任意满足则认为有权限.
     *
     * @return Logical
     */
    Logical logical() default Logical.DEFAULT;

    /**
     * @return 说明
     */
    String[] description() default {};

    /**
     * @return 是否忽略
     */
    boolean ignore() default false;

    @Target({ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @Documented
    @Inherited
    @interface List {
        Dimension[] value() default {};
    }
}