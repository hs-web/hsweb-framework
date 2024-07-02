package org.hswebframework.web.crud.service;

import org.hswebframework.web.crud.entity.TestEntity;
import org.hswebframework.web.crud.events.EntityBeforeModifyEvent;
import org.hswebframework.web.crud.events.EntityCreatedEvent;
import org.hswebframework.web.crud.events.EntityPrepareModifyEvent;
import org.hswebframework.web.id.IDGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TestEntityService extends GenericReactiveCrudService<TestEntity,String> {


    @EventListener
    public void handleEvent(EntityCreatedEvent<TestEntity> event){

        System.out.println(event.getEntity());
    }


    @EventListener
    public void listener(EntityPrepareModifyEvent<TestEntity> event){
        System.out.println(event);
        event.async(Mono.empty());
    }
}
