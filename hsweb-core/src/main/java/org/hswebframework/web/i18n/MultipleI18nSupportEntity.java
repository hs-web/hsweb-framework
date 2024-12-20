package org.hswebframework.web.i18n;

import org.apache.commons.collections4.MapUtils;

import java.util.Collections;
import java.util.Map;

public interface MultipleI18nSupportEntity extends I18nSupportEntity {

    Map<String, Map<String, String>> getI18nMessages();

    @Override
    default Map<String, String> getI18nMessages(String key) {
        Map<String, Map<String, String>> source = getI18nMessages();
        if (MapUtils.isNotEmpty(source)) {
            return Collections.emptyMap();
        }
        return source.get(key);
    }
}
