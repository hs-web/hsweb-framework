package org.hswebframework.web.authorization.cloud.client.feign;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 * @since
 */
@Configuration
@EnableFeignClients("org.hswebframework.web.authorization.cloud.client.feign")
public class FeignAuthorizationAutoConfiguration {
}
