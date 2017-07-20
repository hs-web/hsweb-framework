package org.hswebframework.web.authorization.oauth2.resource;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import java.lang.annotation.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ImportAutoConfiguration(OAuth2ResourceServerAutoConfigruation.class)
public @interface EnableOAuth2ResourceServer {
}
