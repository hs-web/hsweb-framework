package org.hswebframework.web.crud.annotation;

import java.lang.annotation.*;

/**
 * @see org.hswebframework.web.crud.events.EntityModifyEvent
 * @see org.hswebframework.web.crud.events.EntityDeletedEvent
 * @see org.hswebframework.web.crud.events.EntityCreatedEvent
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface EnableEntityEvent {

}
