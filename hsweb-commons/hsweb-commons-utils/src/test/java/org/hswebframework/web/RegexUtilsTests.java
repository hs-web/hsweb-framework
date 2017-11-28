package org.hswebframework.web;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class RegexUtilsTests {

    @Test
    public void test() {
        Arrays.asList('\\', '$', '(', ')', '*', '+', '.', '[', ']', '?', '^', '{', '}', '|')
                .forEach((s) -> assertEquals(RegexUtils.escape(String.valueOf(s)), "\\" + s));

    }
}