package org.hswebframework.web.bean;

import org.hswebframework.utils.time.DateFormatter;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiffTest {

    @Test
    public void mapTest() {
        Map<String, Object> before = new HashMap<>();
        before.put("name", "name");
        before.put("age",21);
        before.put("birthday", DateFormatter.fromString("19910101"));

        Map<String, Object> after = new HashMap<>();
        after.put("name", "name");
        after.put("age", "21");
        after.put("birthday", "1991-01-01");


        List<Diff> diffs = Diff.of(before, after);
        System.out.println(diffs);
        Assert.assertTrue(diffs.isEmpty());

    }
}