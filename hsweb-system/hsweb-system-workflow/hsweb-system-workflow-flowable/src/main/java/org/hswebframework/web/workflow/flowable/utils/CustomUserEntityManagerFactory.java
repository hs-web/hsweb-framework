package org.hswebframework.web.workflow.flowable.utils;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;

public class CustomUserEntityManagerFactory implements SessionFactory {
    private CustomUserEntityManager customUserEntityManager;


    public CustomUserEntityManagerFactory(CustomUserEntityManager customUserEntityManager) {
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