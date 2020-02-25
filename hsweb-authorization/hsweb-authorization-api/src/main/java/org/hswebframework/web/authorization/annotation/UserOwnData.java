package org.hswebframework.web.authorization.annotation;

import java.lang.annotation.*;

/**
 * 声明某个操作支持用户查看自己的数据
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@DataAccessType(id = "user_own_data", name = "用户自己的数据")
public @interface UserOwnData {

}
