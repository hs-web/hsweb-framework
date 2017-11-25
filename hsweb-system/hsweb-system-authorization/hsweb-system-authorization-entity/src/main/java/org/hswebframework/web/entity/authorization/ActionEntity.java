package org.hswebframework.web.entity.authorization;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.commons.entity.CloneableEntity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class ActionEntity implements CloneableEntity {

    private String action;

    private String describe;

    private boolean defaultCheck;

    public ActionEntity(String action) {
        this.action = action;
    }

    @Override
    public ActionEntity clone() {
        try {
            return (ActionEntity) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<ActionEntity> create(String... actions) {
        return Arrays.stream(actions).map(ActionEntity::new).collect(Collectors.toList());
    }

}