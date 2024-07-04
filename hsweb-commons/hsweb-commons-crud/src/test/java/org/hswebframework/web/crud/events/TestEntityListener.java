package org.hswebframework.web.crud.events;

import org.hswebframework.web.crud.entity.EventTestEntity;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

public class TestEntityListener {

    AtomicInteger beforeCreate = new AtomicInteger();
    AtomicInteger beforeDelete = new AtomicInteger();
    AtomicInteger created = new AtomicInteger();
    AtomicInteger deleted = new AtomicInteger();

    AtomicInteger modified = new AtomicInteger();
    AtomicInteger beforeModify = new AtomicInteger();

    AtomicInteger saved = new AtomicInteger();
    AtomicInteger beforeSave = new AtomicInteger();
    AtomicInteger beforeQuery = new AtomicInteger();

    void reset(){
        beforeCreate.set(0);
        beforeDelete.set(0);
        created.set(0);
        deleted.set(0);
        modified.set(0);
        beforeModify.set(0);
        saved.set(0);
        beforeSave.set(0);
        beforeQuery.set(0);

    }

    @EventListener
    public void handleBeforeQuery(EntityBeforeQueryEvent<EventTestEntity> event){
        event.async(Mono.fromRunnable(() -> {
            System.out.println(event);
            beforeQuery.addAndGet(1);
        }));
    }

    @EventListener
    public void handleBeforeSave(EntityBeforeSaveEvent<EventTestEntity> event) {
        event.async(Mono.fromRunnable(() -> {
            System.out.println(event);
            beforeSave.addAndGet(event.getEntity().size());
        }));
    }

    @EventListener
    public void handleBeforeDelete(EntityBeforeModifyEvent<EventTestEntity> event) {
        event.async(Mono.fromRunnable(() -> {
            System.out.println(event);
            beforeModify.addAndGet(event.getBefore().size());
        }));
    }

    @EventListener
    public void handleBeforeDelete(EntityBeforeDeleteEvent<EventTestEntity> event) {
        event.async(Mono.fromRunnable(() -> {
            System.out.println(event);
            beforeDelete.addAndGet(event.getEntity().size());
        }));
    }

    @EventListener
    public void handleBeforeCreated(EntityBeforeCreateEvent<EventTestEntity> event) {
        event.async(Mono.fromRunnable(() -> {
            System.out.println(event);
            beforeCreate.addAndGet(event.getEntity().size());
        }));
    }

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
    public void handlePrepareModify(EntityPrepareModifyEvent<EventTestEntity> event) {
        event.async(Mono.fromRunnable(() -> {
            System.out.println(event);
            for (EventTestEntity eventTestEntity : event.getAfter()) {
                if(eventTestEntity.getName().equals("prepare-xx")){
                    eventTestEntity.setName("prepare-0");
                    eventTestEntity.setAge(null);
                }
            }
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
