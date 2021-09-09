package org.hswebframework.web.crud.events;

import org.hswebframework.web.crud.entity.EventTestEntity;
import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultEntityEventListenerConfigureTest {

    @Test
    public void test() {
        DefaultEntityEventListenerConfigure configure = new DefaultEntityEventListenerConfigure();
        configure.enable(EventTestEntity.class);
        configure.disable(EventTestEntity.class, EntityEventType.create, EntityEventPhase.after);


        assertTrue(configure.isEnabled(EventTestEntity.class));
        assertTrue(configure.isEnabled(EventTestEntity.class, EntityEventType.create, EntityEventPhase.before));

        assertFalse(configure.isEnabled(EventTestEntity.class, EntityEventType.create, EntityEventPhase.after));

    }
}