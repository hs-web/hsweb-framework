package org.hswebframework.web.system.authorization.defaults.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ConfigurationProperties(prefix = "hsweb.permission.initialize")
public class AuthenticationInitializeProperties implements AuthenticationInitializeCustomizer.Context {

    private Set<String> enabledDimensions;

    public boolean isDimensionEnabled(String dimensionType) {
        return enabledDimensions == null || enabledDimensions.contains("*") || enabledDimensions.contains(dimensionType);
    }

    @Override
    public synchronized void enableDimension(String dimensionType) {
        if (enabledDimensions == null) {
            enabledDimensions = new HashSet<>();
        }
        enabledDimensions.add(dimensionType);
    }


}
