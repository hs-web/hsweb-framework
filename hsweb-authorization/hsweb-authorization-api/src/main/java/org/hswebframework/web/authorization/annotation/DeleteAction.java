package org.hswebframework.web.authorization.annotation;

import org.hswebframework.web.authorization.Permission;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@ResourceAction(id = Permission.ACTION_DELETE, name = "删除")
public @interface DeleteAction {

    @AliasFor(annotation = ResourceAction.class,attribute = "dataAccess")
    DataAccess dataAccess() default @DataAccess(ignore = true);
}
