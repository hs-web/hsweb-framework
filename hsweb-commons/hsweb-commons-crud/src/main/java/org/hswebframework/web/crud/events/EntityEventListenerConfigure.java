package org.hswebframework.web.crud.events;

import org.hswebframework.web.api.crud.entity.Entity;

/**
 * 实体事件监听器配置
 * <pre>
 *     configure.enable(MyEntity.class)//启用事件
 *              //禁用某一类事件
 *              .disable(MyEntity.class,EntityEventType.modify,EntityEventPhase.all)
 * </pre>
 *
 * @author zhouhao
 * @since 4.0.12
 */
public interface EntityEventListenerConfigure {

    /**
     * 启用实体类的事件
     *
     * @param entityType 实体类
     * @see org.hswebframework.web.crud.annotation.EnableEntityEvent
     */
    void enable(Class<? extends Entity> entityType);

    /**
     * 禁用实体类事件
     *
     * @param entityType 实体类
     */
    void disable(Class<? extends Entity> entityType);

    /**
     * 启用指定类型的事件
     *
     * @param entityType 实体类型
     * @param type       事件类型
     * @param phases     事件阶段，如果不传则启用全部
     */
    void enable(Class<? extends Entity> entityType,
                EntityEventType type,
                EntityEventPhase... phases);

    /**
     * 禁用指定类型的事件
     *
     * @param entityType 实体类型
     * @param type       事件类型
     * @param phases     事件阶段，如果不传则禁用全部
     */
    void disable(Class<? extends Entity> entityType,
                 EntityEventType type,
                 EntityEventPhase... phases);

    /**
     * 判断实体类是否启用了事件
     *
     * @param entityType 实体类
     * @return 是否启用
     */
    boolean isEnabled(Class<? extends Entity> entityType);

    /**
     * 判断实体类是否启用了指定类型的事件
     *
     * @param entityType 实体类
     * @param type       事件类型
     * @param phase      事件阶段
     * @return 是否启用
     */
    boolean isEnabled(Class<? extends Entity> entityType, EntityEventType type, EntityEventPhase phase);

}
