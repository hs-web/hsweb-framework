package org.hswebframework.web.crud.configuration;

import org.hswebframework.ezorm.core.FeatureId;
import org.hswebframework.ezorm.core.meta.Feature;
import org.hswebframework.ezorm.rdb.events.EventContext;
import org.hswebframework.ezorm.rdb.events.EventListener;
import org.hswebframework.ezorm.rdb.events.EventType;
import org.hswebframework.ezorm.rdb.mapping.events.MappingEventTypes;
import org.hswebframework.web.crud.events.CompositeEventListener;
import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class EasyormEventListenerRegistrationTest {

    @Test
    public void defaultIdListenerDoesNotReplaceEventDispatcher() {
        for (boolean extensionLast : new boolean[]{false, true}) {
            assertDispatcherRegistered(extensionLast);
        }
    }

    private void assertDispatcherRegistered(boolean extensionLast) {
        AtomicInteger entityEvents = new AtomicInteger();
        AtomicInteger extensionEvents = new AtomicInteger();
        EventListener entity = (type, context) -> entityEvents.incrementAndGet();
        EventListener extension = (type, context) -> extensionEvents.incrementAndGet();
        CompositeEventListener dispatcher = new CompositeEventListener();
        dispatcher.addListener(entity);
        dispatcher.addListener(extension);

        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        if (!extensionLast) {
            beans.registerSingleton("extensionListener", extension);
        }
        beans.registerSingleton("easyormEventListener", dispatcher);
        if (extensionLast) {
            beans.registerSingleton("extensionListener", extension);
        }
        EasyormProperties properties = new EasyormProperties();
        properties.setAutoDdl(false);
        var metadata = new EasyormConfiguration().databaseMetadata(
            Optional.empty(), Optional.empty(), beans.getBeanProvider(Feature.class), properties, dispatcher);

        assertSame(dispatcher, metadata.getFeatureNow(EventListener.ID));
        metadata.getFeatureNow(EventListener.ID).onEvent(MappingEventTypes.update_before, EventContext.create());
        assertEquals(1, entityEvents.get());
        assertEquals(1, extensionEvents.get());
    }

    @Test
    public void namedListenerFeaturesRemainRegisteredAndDispatchOnlyOnce() {
        AtomicInteger extensionEvents = new AtomicInteger();
        EventListener extension = new EventListener() {
            @Override
            public String getId() {
                return "extensionEvents";
            }

            @Override
            public void onEvent(EventType type, EventContext context) {
                extensionEvents.incrementAndGet();
            }
        };
        CompositeEventListener dispatcher = new CompositeEventListener();
        dispatcher.addListener(extension);
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        beans.registerSingleton("easyormEventListener", dispatcher);
        beans.registerSingleton("extensionListener", extension);
        EasyormProperties properties = new EasyormProperties();
        properties.setAutoDdl(false);

        var metadata = new EasyormConfiguration().databaseMetadata(
            Optional.empty(), Optional.empty(), beans.getBeanProvider(Feature.class), properties, dispatcher);

        assertSame(dispatcher, metadata.getFeatureNow(EventListener.ID));
        assertSame(extension, metadata.getFeatureNow(FeatureId.of("extensionEvents")));
        metadata.getFeatureNow(EventListener.ID).onEvent(MappingEventTypes.update_after, EventContext.create());
        assertEquals(1, extensionEvents.get());
    }
}
