package org.hswebframework.web.authorization.annotation;

import org.hswebframework.web.authorization.Permission;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@ResourceAction(id = Permission.ACTION_QUERY, name = "查询")
public @interface QueryAction {

    @AliasFor(annotation = ResourceAction.class,attribute = "dataAccess")
    DataAccess dataAccess() default @DataAccess(ignore = true);

}
