package org.hswebframework.web.authorization.cloud;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zhouhao
 * @since
 */
@Configuration
public class AuthorizationClientAutoConfiguration implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attrs = importingClassMetadata.getAnnotationAttributes(EnableAuthorizationClient.class.getName());

        EnableAuthorizationClient.Type type = (EnableAuthorizationClient.Type) attrs.get("type");
        List<String> classNames = new ArrayList<>();

        if (type != null) {
            switch (type) {
                case Feign:
                    classNames.add("org.hswebframework.web.authorization.cloud.client.feign.FeignAutoConfiguration");
                    break;
                case Auto:
                default:
                    try {
                        Class.forName("org.springframework.cloud.netflix.feign.FeignClient");
                        classNames.add("org.hswebframework.web.authorization.cloud.client.feign.FeignAutoConfiguration");
                    } catch (ClassNotFoundException e) {
                        // load redis not support yet
                        throw new UnsupportedOperationException("please import and config feign");
                    }
                    break;
            }
        }
        return classNames.toArray(new String[classNames.size()]);
    }
}
