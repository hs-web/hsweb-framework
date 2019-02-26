package org.hswebframework.web.audit;

import java.lang.annotation.*;

/**
 * 审计信息注解,用于在修改更新数据的时候记录被修改的内容.
 * @since 3.0.7
 *
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audit {

    //审计信息简介,如功能,字段.
    String value();

    //审计信息的说明
    String comment() default "";

    //是否忽略审计
    boolean ignore() default false;

    //字段生效策略,如果为MANUAL,修改数据时,字段将不会立即修改,而是需要手动审核通过后生效.
    Strategy strategy() default Strategy.AUTO;

    //如果设置了审核权限,当进行审核或者回退的时候将进行权限验证.
    //如: permission:user:audit
    //role:admin
    String auditPermission() default "";
}
