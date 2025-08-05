package org.hswebframework.web.authorization.annotation;


import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 注解根据角色维度进行权限控制,具有权限的用户才可访问对应的方法.
 *
 * <pre>{@code
 *    @RequiresRoles("admin")
 *    public Mono<Void> handleRequest(){
 *
 *    }
 * }</pre>
 *
 * @author zhouhao
 * @see Dimension
 * @since 4.0
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Dimension(type = "role")
@Repeatable(RequiresRoles.List.class)
public @interface RequiresRoles {

    /**
     * @return 角色ID
     */
    @AliasFor(annotation = Dimension.class, attribute = "id")
    String[] value() default {};

    /**
     * 多个角色时的判断逻辑
     * @return Logical
     */
    @AliasFor(annotation = Dimension.class, attribute = "logical")
    Logical logical() default Logical.DEFAULT;

    @Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
    @Retention(RUNTIME)
    @Documented
    @Inherited
    @Dimension.List()
    @interface List {
        RequiresRoles[] value();
    }
}
