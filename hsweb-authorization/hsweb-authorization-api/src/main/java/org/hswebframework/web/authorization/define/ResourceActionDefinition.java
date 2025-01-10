package org.hswebframework.web.authorization.define;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.i18n.LocaleUtils;

import java.util.HashMap;
import java.util.Map;

import static org.hswebframework.web.authorization.define.ResourceDefinition.supportLocale;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class ResourceActionDefinition {
    private String id;

    private String name;

    private String description;

    private Map<String, Map<String, String>> i18nMessages = new HashMap<>();

    private DataAccessDefinition dataAccess = new DataAccessDefinition();


    private final static String resolveActionPrefix = "hswebframework.web.system.action.";

    public ResourceActionDefinition copy() {
        return FastBeanCopier.copy(this, ResourceActionDefinition::new);
    }

    private Map<String, Map<String, String>> buildI18nMessage(String id, String name) {
        Map<String, String> nameMap = new HashMap<>();
        Map<String, String> describeMap = new HashMap<>();
        supportLocale.forEach(locale -> {
            nameMap.put(locale.getLanguage(), LocaleUtils.resolveMessage(resolveActionPrefix + id, locale, name));
            describeMap.put(locale.getLanguage(), LocaleUtils.resolveMessage(resolveActionPrefix + id, locale, name));
        });
        i18nMessages.put("name", nameMap);
        i18nMessages.put("description", describeMap);
        return i18nMessages;
    }

    public ResourceActionDefinition addI18nMessage(String id, String name) {
        this.setI18nMessages(buildI18nMessage(id, name));
        return this;
    }

}
