package org.hswebframework.web.authorization.define;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.web.authorization.annotation.Logical;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.i18n.I18nSupportUtils;
import org.hswebframework.web.i18n.MultipleI18nSupportEntity;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class ResourceDefinition implements MultipleI18nSupportEntity {
    private String id;

    private String name;

    private String description;

    private Set<ResourceActionDefinition> actions = new HashSet<>();

    private List<String> group;

    private Map<String, Map<String, String>> i18nMessages;

    @Setter(value = AccessLevel.PRIVATE)
    @JsonIgnore
    private volatile Set<String> actionIds;

    private Logical logical = Logical.DEFAULT;

    private Phased phased = Phased.before;

    public final static List<Locale> supportLocale = new ArrayList<>();

    static {
        supportLocale.add(Locale.CHINESE);
        supportLocale.add(Locale.ENGLISH);
    }


    private final static String resolvePermissionPrefix = "hswebframework.web.system.permission.";

    public static ResourceDefinition of(String id, String name) {
        ResourceDefinition definition = new ResourceDefinition();
        definition.setId(id);
        definition.setName(name);
        return definition;
    }

    public Map<String, Map<String, String>> getI18nMessages() {
        if (org.springframework.util.CollectionUtils.isEmpty(i18nMessages)) {
            this.i18nMessages = I18nSupportUtils
                    .putI18nMessages(
                            resolvePermissionPrefix + this.id, "text", supportLocale, null, this.i18nMessages
                    );
        }
        return i18nMessages;
    }

    public ResourceDefinition copy() {
        ResourceDefinition definition = FastBeanCopier.copy(this, ResourceDefinition::new);
        definition.setActions(actions.stream().map(ResourceActionDefinition::copy).collect(Collectors.toSet()));
        return definition;
    }

    public ResourceDefinition addAction(String id, String name) {
        ResourceActionDefinition action = new ResourceActionDefinition();
        action.setId(id);
        action.setName(name);
        return addAction(action);
    }

    public synchronized ResourceDefinition addAction(ResourceActionDefinition action) {
        actionIds = null;
        ResourceActionDefinition old = getAction(action.getId()).orElse(null);
        if (old != null) {
            old.getDataAccess().getDataAccessTypes()
               .addAll(action.getDataAccess().getDataAccessTypes());
        }
        actions.add(action);
        return this;
    }

    public Optional<ResourceActionDefinition> getAction(String action) {
        return actions.stream()
                      .filter(act -> act.getId().equalsIgnoreCase(action))
                      .findAny();
    }

    public Set<String> getActionIds() {
        if (actionIds == null) {
            actionIds = this.actions
                    .stream()
                    .map(ResourceActionDefinition::getId)
                    .collect(Collectors.toSet());
        }
        return actionIds;
    }

    @JsonIgnore
    public List<ResourceActionDefinition> getDataAccessAction() {
        return actions.stream()
                      .filter(act -> CollectionUtils.isNotEmpty(act.getDataAccess().getDataAccessTypes()))
                      .collect(Collectors.toList());
    }

    public boolean hasDataAccessAction() {
        return actions.stream()
                      .anyMatch(act -> CollectionUtils.isNotEmpty(act.getDataAccess().getDataAccessTypes()));
    }

    public boolean hasAction(Collection<String> actions) {
        if (CollectionUtils.isEmpty(this.actions)) {
            return true;
        }

        if (CollectionUtils.isEmpty(actions)) {
            return false;
        }

        if (logical == Logical.AND) {
            return getActionIds().containsAll(actions);
        }
        return getActionIds().stream().anyMatch(actions::contains);
    }
}
