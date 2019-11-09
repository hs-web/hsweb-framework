package org.hswebframework.web.crud.events;

import org.hswebframework.web.crud.entity.EventTestEntity;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TestEntityListener {

    AtomicInteger created = new AtomicInteger();
    AtomicInteger deleted = new AtomicInteger();

    AtomicInteger modified = new AtomicInteger();


    @EventListener
    public void handleCreated(EntityCreatedEvent<EventTestEntity> event) {
        System.out.println(event);
        created.addAndGet(event.getEntity().size());
    }

    @EventListener
    public void handleCreated(EntityDeletedEvent<EventTestEntity> event) {
        System.out.println(event);
        deleted.addAndGet(event.getEntity().size());
    }

    @EventListener
    public void handleModify(EntityModifyEvent<EventTestEntity> event) {
        System.out.println(event);
        modified.addAndGet(event.getAfter().size());
    }
}
