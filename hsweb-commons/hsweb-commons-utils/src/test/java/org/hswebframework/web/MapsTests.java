package org.hswebframework.web;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class MapsTests {

    @Test
    public void testCreateMap() {
        assertEquals(Maps.buildMap()
                .put("1", 1)
                .get().get("1"), 1);

        assertEquals(Maps.buildMap(new HashMap<>())
                .put("1", 1)
                .get().get("1"), 1);

        assertEquals(Maps.buildMap(HashMap::new)
                .put("1", 1)
                .get().get("1"), 1);

    }

}