package org.hswebframework.web.authorization.annotation;

import org.hswebframework.web.authorization.Permission;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@ResourceAction(id = Permission.ACTION_ADD, name = "新增")
public @interface CreateAction {

    @AliasFor(annotation = ResourceAction.class,attribute = "dataAccess")
    DataAccess dataAccess() default @DataAccess(ignore = true);
}
