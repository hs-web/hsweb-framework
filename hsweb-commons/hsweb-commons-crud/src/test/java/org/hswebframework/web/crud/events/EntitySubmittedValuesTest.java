package org.hswebframework.web.crud.events;

import org.hswebframework.ezorm.rdb.events.EventContext;
import org.hswebframework.ezorm.rdb.mapping.EntityColumnMapping;
import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.hswebframework.ezorm.rdb.mapping.events.MappingContextKeys;
import org.hswebframework.ezorm.rdb.mapping.events.MappingEventTypes;
import org.hswebframework.web.api.crud.entity.ExtendableEntity;
import org.hswebframework.web.event.GenericsPayloadApplicationEvent;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class EntitySubmittedValuesTest {
    @Test
    public void testNullableExtensionsDoNotBreakOrdinaryModifyEvent() {
        DefaultEntityEventListenerConfigure configure = new DefaultEntityEventListenerConfigure();
        configure.enable(NullableExtensions.class);
        List<Object> published = new ArrayList<>();
        EntityEventListener listener = new EntityEventListener(published::add, configure);
        EntityColumnMapping mapping = mock(EntityColumnMapping.class);
        doReturn(NullableExtensions.class).when(mapping).getEntityType();
        List<Object> before = List.of(new NullableExtensions());
        List<Object> after = List.of(new NullableExtensions());
        Map<String, Object> columns = new LinkedHashMap<>();
        columns.put("name", "updated");
        columns.put("description", null);
        // Use the ordinary update-after context without a CRUD interceptor or submittedValues key.
        EventContext context = EventContext.create()
            .set(MappingContextKeys.repository, mock(SyncRepository.class))
            .set(MappingContextKeys.columnMapping, mapping)
            .set(MappingContextKeys.instance(new NullableExtensions()))
            .set(MappingContextKeys.updateColumnInstance, columns)
            .set(EntityEventListener.readyToUpdateBeforeContextKey, before)
            .set(EntityEventListener.readyToUpdateAfterContextKey, after);

        listener.onEvent(MappingEventTypes.update_after, context);

        assertEquals(1, published.size());
        EntityModifyEvent<?> event = (EntityModifyEvent<?>)
            ((GenericsPayloadApplicationEvent<?>) published.get(0)).getPayload();
        assertSame(before, event.getBefore());
        assertSame(after, event.getAfter());
        assertEquals(columns, event.getSubmittedValues());
        columns.put("name", "later");
        assertEquals("updated", event.getSubmittedValues().get("name"));
        assertTrue(event.getSubmittedValues().containsKey("description"));
        assertNull(event.getSubmittedValues().get("description"));
    }

    public static class NullableExtensions extends ExtendableEntity<String> {
        @Override
        public Map<String, Object> extensions() {
            return null;
        }
    }

    @Test
    public void testOriginalPatchSurvivesOmissionAndSnapshotsContainersAndNulls() {
        EntityEventListener listener = new EntityEventListener(event -> { }, new DefaultEntityEventListenerConfigure());
        ExtendableEntity<String> input = new ExtendableEntity<>();
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("clear", null);
        nested.put("list", new ArrayList<>(List.of("before")));
        input.setExtension("virtual", nested);
        Map<String, Object> original = new LinkedHashMap<>();
        original.put("name", null);
        EventContext context = EventContext.create()
            .set(MappingContextKeys.instance(input))
            .set(MappingContextKeys.submittedValues, original)
            .set(MappingContextKeys.updateColumnInstance, Map.of());

        Map<String, Object> submitted = listener.submittedValues(context);
        nested.put("clear", "later");
        original.put("name", "later");
        assertTrue(submitted.containsKey("name"));
        assertNull(submitted.get("name"));
        assertNull(((Map<?, ?>) submitted.get("virtual")).get("clear"));
        assertThrows(UnsupportedOperationException.class, () -> submitted.put("new", "value"));
        assertThrows(UnsupportedOperationException.class, () -> ((Map<String, Object>) submitted.get("virtual")).put("new", "value"));
        assertSame(submitted, listener.submittedValues(context));
        assertEquals(submitted, new EntityPrepareModifyEvent<>(List.of(), List.of(), ExtendableEntity.class, submitted).getSubmittedValues());
        assertEquals(submitted, new EntityBeforeModifyEvent<>(List.of(), List.of(), ExtendableEntity.class, submitted).getSubmittedValues());
        assertEquals(submitted, new EntityModifyEvent<>(List.of(), List.of(), ExtendableEntity.class, submitted).getSubmittedValues());
    }

    @Test
    public void testExistingConstructorsKeepEmptySubmissionContract() {
        assertTrue(new EntityPrepareModifyEvent<>(List.of(), List.of(), ExtendableEntity.class).getSubmittedValues().isEmpty());
        assertTrue(new EntityBeforeModifyEvent<>(List.of(), List.of(), ExtendableEntity.class).getSubmittedValues().isEmpty());
        assertTrue(new EntityModifyEvent<>(List.of(), List.of(), ExtendableEntity.class).getSubmittedValues().isEmpty());
    }
}
