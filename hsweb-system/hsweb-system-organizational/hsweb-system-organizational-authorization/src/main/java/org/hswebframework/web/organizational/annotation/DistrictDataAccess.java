package org.hswebframework.web.organizational.annotation;

import org.hswebframework.web.authorization.annotation.DataAccessType;

import java.lang.annotation.*;

/**
 * @see org.hswebframework.web.organizational.DistrictAttachEntity
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@DataAccessType(id = "district", name = "行政区划")
public @interface DistrictDataAccess {

}
