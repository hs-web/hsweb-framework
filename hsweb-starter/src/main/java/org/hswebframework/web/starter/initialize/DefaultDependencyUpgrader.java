package org.hswebframework.web.starter.initialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 */
public class DefaultDependencyUpgrader implements DependencyUpgrader {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    Dependency installed;
    Dependency  dependency;
    List<Map<String, Object>> shouldUpdateVersionList;
    private Map<String, Object> context;
    private boolean             firstInstall;

    public DefaultDependencyUpgrader(Dependency installed, Dependency dependency, Map<String, Object> context) {
        this.firstInstall = installed == null;
        if (firstInstall) {
            this.installed = dependency;
        } else {
            this.installed = installed;
        }
        this.context = context;
        this.dependency = dependency;
    }

    @Override
    public DependencyUpgrader filter(List<Map<String, Object>> versions) {
        shouldUpdateVersionList = versions.stream()
                .filter(map -> {
                    String ver = (String) map.get("version");
                    if (null == ver) {
                        return false;
                    }
                    //首次安装
                    if (firstInstall) {
                        return true;
                    }
                    //相同版本
                    if (installed.compareTo(dependency) == 0) {
                        return false;
                    }

                    return installed.compareTo(new SystemVersion(ver)) < 0;
                })
                .sorted(Comparator.comparing(m -> new SystemVersion((String) m.get("version"))))
                .collect(Collectors.toList());
        return this;
    }

    @Override
    public void upgrade(CallBack callBack) {
        shouldUpdateVersionList.forEach(context -> {
            if (this.context != null) {
                context.putAll(this.context);
            }
            if (logger.isInfoEnabled()) {
                logger.info("upgrade [{}/{}] to version:{} {}", dependency.getGroupId(), dependency.getArtifactId(), context.get("version"), dependency.getWebsite());
            }
            callBack.execute(context);
        });
    }

}
