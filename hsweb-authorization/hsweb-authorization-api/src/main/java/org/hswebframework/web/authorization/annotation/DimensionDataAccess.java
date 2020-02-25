package org.hswebframework.web.authorization.annotation;

import org.hswebframework.web.authorization.define.Phased;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@DataAccessType(id = "dimension", name = "维度数据权限")
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Authorize
public @interface DimensionDataAccess {

    Mapping[] mapping() default {};

    @AliasFor(annotation = Authorize.class)
    Phased phased() default Phased.before;

    @AliasFor(annotation = DataAccessType.class)
    boolean ignore() default false;

    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @interface Mapping {
        String dimensionType();

        String property();

        int idParamIndex() default -1;
    }

}
