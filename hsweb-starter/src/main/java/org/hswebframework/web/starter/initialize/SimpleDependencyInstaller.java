package org.hswebframework.web.starter.initialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author zhouhao
 */
public class SimpleDependencyInstaller implements DependencyInstaller {
    Dependency dependency;
    CallBack installer;
    CallBack upgrader;
    CallBack unInstaller;
    CallBack initializer;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public SimpleDependencyInstaller() {
    }

    public Dependency getDependency() {
        return dependency;
    }

    public void doInstall(Map<String, Object> context) {
        if (installer != null) {
            if (logger.isInfoEnabled()) {
                logger.info("install [{}/{}]", dependency.getGroupId(), dependency.getArtifactId());
            }
            installer.execute(context);
        }
    }

    public void doInitialize(Map<String, Object> context) {
        if (initializer != null) {
            if (logger.isInfoEnabled()) {
                logger.info("initialize [{}/{}]", dependency.getGroupId(), dependency.getArtifactId());
            }
            initializer.execute(context);
        }
    }

    public void doUnInstall(Map<String, Object> context) {
        if (unInstaller != null) {
            unInstaller.execute(context);
        }
    }

    public void doUpgrade(Map<String, Object> context, Dependency installed) {
        DefaultDependencyUpgrader defaultDependencyUpgrader =
                new DefaultDependencyUpgrader(installed, dependency, context);
        context.put("upgrader", defaultDependencyUpgrader);
        if (upgrader != null) {
            upgrader.execute(context);
        }
    }

    @Override
    public DependencyInstaller setup(Dependency dependency) {
        this.dependency = dependency;
        return this;
    }

    @Override
    public DependencyInstaller onInstall(CallBack callBack) {
        this.installer = callBack;
        return this;
    }

    @Override
    public DependencyInstaller onUpgrade(CallBack callBack) {
        this.upgrader = callBack;
        return this;
    }

    @Override
    public DependencyInstaller onUninstall(CallBack callBack) {
        this.unInstaller = callBack;
        return this;
    }

    @Override
    public DependencyInstaller onInitialize(CallBack initializeCallBack) {
        this.initializer = initializeCallBack;
        return this;
    }
}
