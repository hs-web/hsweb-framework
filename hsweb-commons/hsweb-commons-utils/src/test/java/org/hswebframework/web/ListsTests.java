package org.hswebframework.web;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;

import static org.junit.Assert.*;

public class ListsTests {

    @Test
    public void testCreate() {

        assertEquals(Lists.buildList(2).add(1)
                .get().get(0), (Integer) 2);


        assertEquals(Lists.buildList(new ArrayList<>()).add(2,1)
                .get().get(0), 2);

        assertEquals(Lists.buildList(ArrayList::new)
                .add(2,1)
                .get()
                .get(0), 2);

    }
}