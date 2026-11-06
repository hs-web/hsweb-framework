package org.hswebframework.web.crud.events;

import org.hswebframework.ezorm.rdb.events.EventContext;
import org.hswebframework.ezorm.rdb.mapping.events.MappingContextKeys;
import org.hswebframework.web.api.crud.entity.ExtendableEntity;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class EntitySubmittedValuesTest {
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
