package org.hswebframework.web.authorization.annotation;

import org.hswebframework.web.authorization.Permission;

import java.lang.annotation.*;

/**
 * 继承{@link ResourceAction},提供统一的id定义
 *
 * @author zhouhao
 * @since 4.0
 */
@Target({ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@ResourceAction(id = Permission.ACTION_SAVE, name = "保存")
public @interface SaveAction {


}
