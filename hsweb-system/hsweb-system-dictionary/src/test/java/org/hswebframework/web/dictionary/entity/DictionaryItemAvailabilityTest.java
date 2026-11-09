package org.hswebframework.web.dictionary.entity;

import org.junit.Test;

import static org.junit.Assert.*;

public class DictionaryItemAvailabilityTest {
    @Test
    public void availabilityUsesDictionaryStatusWithoutAffectingStoredIdentity() {
        DictionaryItemEntity item = new DictionaryItemEntity();
        item.setValue("grade-A");
        item.setOrdinal(62);
        item.setStatus((byte) 1);
        assertTrue(item.isEnabled());
        item.setStatus((byte) 0);
        assertFalse(item.isEnabled());
        assertEquals("grade-A", item.getValue());
        assertEquals(62, item.ordinal());
    }
}
