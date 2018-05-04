package org.hswebframework.web.dev.tools;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import java.lang.annotation.*;

/**
 * @author zhouhao
 * @since 3.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ImportAutoConfiguration(DevToolsAutoConfiguration.class)
public @interface EnableDevTools {
}
