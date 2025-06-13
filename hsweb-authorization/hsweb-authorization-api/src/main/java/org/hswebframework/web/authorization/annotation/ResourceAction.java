package org.hswebframework.web.authorization.annotation;


import org.hswebframework.web.authorization.Permission;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 对资源操作的描述,通常用来进行权限控制.
 * <p>
 * 在Controller方法上添加此注解,来声明根据权限操作{@link Permission#getActions()}进行权限控制.
 * <p>
 * 可以使用注解继承的方式来统一定义操作:
 * <pre>{@code
 * @Target(ElementType.METHOD)
 * @Retention(RetentionPolicy.RUNTIME)
 * @Inherited
 * @Documented
 * @ResourceAction(id = "create", name = "新增")
 * public @interface CreateAction {
 *
 * }
 * }
 * </pre>
 *
 * @see CreateAction
 * @see DeleteAction
 * @see SaveAction
 * @see org.hswebframework.web.authorization.Authentication
 * @see Permission#getActions()
 */
@Target({ANNOTATION_TYPE, FIELD, METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Repeatable(ResourceAction.List.class)
public @interface ResourceAction {
    /**
     * 操作标识
     *
     * @return 操作标识
     * @see Permission#getActions()
     */
    String id();

    /**
     * @return 操作名称
     */
    String name();

    /**
     * @return 操作说明
     */
    String[] description() default {};

    /**
     * @return 多个操作时的判断逻辑
     */
    Logical logical() default Logical.DEFAULT;

    @Target({ANNOTATION_TYPE, FIELD, METHOD})
    @Retention(RUNTIME)
    @Documented
    @Inherited
    @interface List {
        ResourceAction[] value();
    }

}
