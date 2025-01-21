package org.hswebframework.web.i18n;

import org.junit.Test;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;

public class I18nSupportUtilsTest {


    @Test
    public void test() {
        Map<String, Map<String, String>> container = I18nSupportUtils
            .putI18nMessages("message.test.a",
                             "a",
                             Arrays.asList(Locale.CHINESE, Locale.ENGLISH),
                             "Test",
                             null
            );
        System.out.println(container);
        assertNotNull(container);
        assertNotNull(container.get("a"));
        assertTrue(container.get("a").containsKey("zh"));
        assertTrue(container.get("a").containsKey("en"));

    }

}