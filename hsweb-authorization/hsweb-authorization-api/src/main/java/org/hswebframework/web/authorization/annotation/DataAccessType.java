package org.hswebframework.web.authorization.annotation;

import org.hswebframework.web.authorization.access.DataAccessConfiguration;
import org.hswebframework.web.authorization.access.DataAccessController;

import java.lang.annotation.*;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DataAccessType {

    String id(); //标识

    String name(); //名称

    String[] description() default {};

    /**
     * @see DataAccessController
     */
    Class<? extends DataAccessController> controller() default DataAccessController.class;

    Class<? extends DataAccessConfiguration> configuration() default DataAccessConfiguration.class;

    boolean ignore() default false;
}