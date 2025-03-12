package org.hswebframework.web.i18n;

import org.apache.commons.collections4.MapUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class I18nSupportUtils {

    public static Map<String, Map<String, String>> putI18nMessages(String i18nKey,
                                                                   String property,
                                                                   Collection<Locale> locales,
                                                                   String defaultMsg,
                                                                   Map<String, Map<String, String>> container) {
        if (container == null) {
            container = new HashMap<>();
        }

        container.compute(
            property,
            (p, c) -> {
                Map<String, String> msg = putI18nMessages(i18nKey, locales, defaultMsg, c);
                //为空不存储
                return MapUtils.isEmpty(msg) ? null : msg;
            });

        return container;
    }

    public static Map<String, String> putI18nMessages(String i18nKey,
                                                      Collection<Locale> locales,
                                                      String defaultMsg,
                                                      Map<String, String> container) {
        if (container == null) {
            container = new HashMap<>();
        }

        for (Locale locale : locales) {
            String msg = LocaleUtils.resolveMessage(i18nKey, locale, defaultMsg);
            if (StringUtils.hasText(msg)) {
                container.put(locale.toString(), msg);
            }
        }

        return container;
    }


}
