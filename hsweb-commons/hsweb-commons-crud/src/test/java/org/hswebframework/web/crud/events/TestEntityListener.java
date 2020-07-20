package org.hswebframework.web.crud.events;

import org.hswebframework.web.crud.entity.EventTestEntity;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TestEntityListener {

    AtomicInteger created = new AtomicInteger();
    AtomicInteger deleted = new AtomicInteger();

    AtomicInteger modified = new AtomicInteger();

    AtomicInteger saved = new AtomicInteger();

    @EventListener
    public void handleCreated(EntityCreatedEvent<EventTestEntity> event) {
        event.async(Mono.fromRunnable(() -> {
            System.out.println(event);
            created.addAndGet(event.getEntity().size());
        }));
    }

    @EventListener
    public void handleCreated(EntityDeletedEvent<EventTestEntity> event) {
        event.async(Mono.fromRunnable(() -> {
            System.out.println(event);
            deleted.addAndGet(event.getEntity().size());
        }));
    }

    @EventListener
    public void handleModify(EntityModifyEvent<EventTestEntity> event) {
        event.async(Mono.fromRunnable(() -> {
            System.out.println(event);
            modified.addAndGet(event.getAfter().size());
        }));
    }

    @EventListener
    public void handleSave(EntitySavedEvent<EventTestEntity> event) {
        event.async(Mono.fromRunnable(() -> {
            System.out.println(event);
            saved.addAndGet(event.getEntity().size());
        }));
    }
}
