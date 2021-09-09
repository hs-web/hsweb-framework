package org.hswebframework.web.crud.events;

/**
 * 实体事件监听器自定义接口，用于自定义实体事件
 *
 * @author zhouhao
 * @see EntityEventListenerConfigure
 * @since 4.0.12
 */
public interface EntityEventListenerCustomizer {

    /**
     * 执行自定义
     * @param configure configure
     */
    void customize(EntityEventListenerConfigure configure);

}
