package org.hswebframework.web.i18n;

import java.util.Map;

public interface SingleI18nSupportEntity extends I18nSupportEntity {

    Map<String, String> getI18nMessages();

    default Map<String, String> getI18nMessages(String key) {
        return getI18nMessages();
    }

}
