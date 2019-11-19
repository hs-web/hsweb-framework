package org.hswebframework.web.authorization.basic.aop;

import org.hswebframework.web.authorization.annotation.DimensionDataAccess;
import org.hswebframework.web.authorization.define.Phased;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@DimensionDataAccess
@DimensionDataAccess.Mapping(dimensionType = "role", property = "roleId")
public @interface TestDataAccess {

    @AliasFor(annotation = DimensionDataAccess.Mapping.class)
    int idParamIndex() default -1;

    @AliasFor(annotation = DimensionDataAccess.class)
    Phased phased() default Phased.before;

}
