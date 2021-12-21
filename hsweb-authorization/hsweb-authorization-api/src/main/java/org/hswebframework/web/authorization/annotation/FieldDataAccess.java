package org.hswebframework.web.authorization.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @deprecated 已弃用
 */
@DataAccessType(id = "FIELD_DENY", name = "字段权限")
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Deprecated
public @interface FieldDataAccess {

    @AliasFor(annotation = DataAccessType.class)
    boolean ignore() default false;
}
