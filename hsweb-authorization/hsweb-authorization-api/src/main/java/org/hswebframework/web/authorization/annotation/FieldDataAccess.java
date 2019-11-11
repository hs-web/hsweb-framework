package org.hswebframework.web.authorization.annotation;

import java.lang.annotation.*;

@DataAccessType(id = "DENY_FIELDS", name = "字段权限")
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
public @interface FieldDataAccess {

}
