package org.hswebframework.web.authorization.cloud.client.feign;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 3.0
 */
@Configuration
@EnableFeignClients("org.hswebframework.web.authorization.cloud.client.feign")
public class FeignAuthorizationAutoConfiguration {
}
