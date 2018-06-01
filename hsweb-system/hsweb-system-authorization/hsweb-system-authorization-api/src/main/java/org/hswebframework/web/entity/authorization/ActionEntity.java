package org.hswebframework.web.entity.authorization;

import lombok.*;
import org.hswebframework.web.commons.entity.CloneableEntity;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActionEntity implements CloneableEntity {

    private static final long serialVersionUID = -5756333786703175612L;

    private String action;

    private String describe;

    private boolean defaultCheck;

    public ActionEntity(String action) {
        this.action = action;
    }

    @Override
    @SneakyThrows
    public ActionEntity clone() {
        return (ActionEntity) super.clone();
    }

    public static List<ActionEntity> create(String... actions) {
        return Arrays.stream(actions).map(ActionEntity::new).collect(Collectors.toList());
    }

    @Override
    public int hashCode() {
        return getAction().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ActionEntity && obj.hashCode() == hashCode();
    }

    @Override
    public String toString() {
        return action + (StringUtils.hasText(describe) ? "(" + describe + ")" : "");
    }
}