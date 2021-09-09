package org.hswebframework.web.crud.annotation;

import org.hswebframework.web.crud.events.EntityEventType;

import java.lang.annotation.*;

//import static org.hswebframework.web.crud.annotation.EnableEntityEvent.Feature.*;

/**
 * 在实体类上添加此注解，表示开启实体操作事件，当实体类发生类修改，更新，删除等操作时，会触发事件。
 * 可以通过spring event监听事件:
 * <pre>
 *     &#64EventListener
 *     public void handleEvent(EntitySavedEvent&lt;UserEntity&gt; event){
 *         event
 *         .async( //组合响应式操作
 *              deleteByUser(event.getEntity())
 *         )
 *     }
 * </pre>
 *
 * @see org.hswebframework.web.crud.events.EntityModifyEvent
 * @see org.hswebframework.web.crud.events.EntityDeletedEvent
 * @see org.hswebframework.web.crud.events.EntityCreatedEvent
 * @see org.hswebframework.web.crud.events.EntitySavedEvent
 * @see org.hswebframework.web.crud.events.EntityBeforeSaveEvent
 * @see org.hswebframework.web.crud.events.EntityBeforeModifyEvent
 * @see org.hswebframework.web.crud.events.EntityBeforeDeleteEvent
 * @see org.hswebframework.web.crud.events.EntityBeforeCreateEvent
 * @see org.hswebframework.web.crud.events.EntityBeforeQueryEvent
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface EnableEntityEvent {

    EntityEventType[] value() default {
            EntityEventType.create,
            EntityEventType.delete,
            EntityEventType.modify,
            EntityEventType.save
    };

}
