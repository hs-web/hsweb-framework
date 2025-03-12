package org.hswebframework.web.i18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.junit.Test;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;

public class MultipleI18nSupportEntityTest {


    @Test
    @SneakyThrows
    public void testJson() {
        MultipleI18nSupportEntityTestEntity entity = new MultipleI18nSupportEntityTestEntity();

        entity.setI18nMessages(Collections.singletonMap("name", Collections.singletonMap("zh", "名称")));

        String msg = LocaleUtils.doWith(
            entity,
            Locale.CHINA,
            (e, l) -> e.getI18nMessage("name", "123"));

        assertNotEquals("123", msg);
    }

    @Getter
    @Setter
    public static class MultipleI18nSupportEntityTestEntity implements MultipleI18nSupportEntity {

        private Map<String, Map<String, String>> i18nMessages;

        @Override
        public Map<String, Map<String, String>> getI18nMessages() {
            return i18nMessages;
        }
    }
}