package org.hswebframework.web.workflow.flowable.utils;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2016/3/19 0019.
 */
public class CustomGroupEntityManagerFactory implements SessionFactory {

    private CustomGroupEntityManager customGroupEntityManager;


    public CustomGroupEntityManagerFactory(CustomGroupEntityManager customGroupEntityManager) {
        this.customGroupEntityManager = customGroupEntityManager;
    }

    @Override
    public Class<?> getSessionType() {
        // 返回原始的GroupEntityManager类型
        return GroupEntityManager.class;
    }

    @Override
    public Session openSession() {
        // 返回自定义的GroupEntityManager实例
        return customGroupEntityManager;
    }
}
