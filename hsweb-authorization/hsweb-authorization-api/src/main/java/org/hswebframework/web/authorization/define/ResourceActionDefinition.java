package org.hswebframework.web.authorization.define;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.i18n.I18nSupportUtils;
import org.hswebframework.web.i18n.MultipleI18nSupportEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.hswebframework.web.authorization.define.ResourceDefinition.supportLocale;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class ResourceActionDefinition implements MultipleI18nSupportEntity {
    private String id;

    private String name;

    private String description;

    private Map<String, Map<String, String>> i18nMessages;

    @Deprecated
    private DataAccessDefinition dataAccess = new DataAccessDefinition();


    private final static String resolveActionPrefix = "hswebframework.web.system.action.";

    public ResourceActionDefinition copy() {
        return FastBeanCopier.copy(this, ResourceActionDefinition::new);
    }

    public Map<String, Map<String, String>> getI18nMessages() {
        if (org.springframework.util.CollectionUtils.isEmpty(i18nMessages)) {
            this.i18nMessages = I18nSupportUtils
                    .putI18nMessages(
                            resolveActionPrefix + this.id, "name", supportLocale, null, this.i18nMessages
                    );
        }
        return i18nMessages;
    }
}
