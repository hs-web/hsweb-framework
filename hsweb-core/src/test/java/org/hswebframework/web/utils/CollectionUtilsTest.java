package org.hswebframework.web.utils;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class CollectionUtilsTest {

    @Test
    public void test() {
        Map<Integer, Integer> maps = CollectionUtils.pairingArrayMap(1, 2, 3, 4, 5);
        assertEquals(2, maps.size());
        assertEquals(Integer.valueOf(2), maps.get(1));
        assertEquals(Integer.valueOf(4), maps.get(3));
    }
}