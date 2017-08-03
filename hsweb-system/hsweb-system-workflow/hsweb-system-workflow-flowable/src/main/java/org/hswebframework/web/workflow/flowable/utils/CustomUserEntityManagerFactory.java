package org.hswebframework.web.workflow.flowable.utils;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.springframework.beans.factory.annotation.Autowired;

public class CustomUserEntityManagerFactory implements SessionFactory {
    private CustomUserEntityManager customUserEntityManager;

    @Autowired
    public void setCustomUserEntityManager(CustomUserEntityManager customUserEntityManager) {
        this.customUserEntityManager = customUserEntityManager;
    }

    @Override
    public Class<?> getSessionType() { // 返回引擎的实体管理器接口
        return UserIdentityManager.class;
    }

    @Override
    public Session openSession() {
        return customUserEntityManager;
    }
}