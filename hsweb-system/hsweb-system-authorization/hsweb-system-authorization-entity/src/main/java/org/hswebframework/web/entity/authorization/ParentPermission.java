package org.hswebframework.web.entity.authorization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.commons.entity.Entity;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParentPermission implements Entity {

    private static final long serialVersionUID = -7099575758680437572L;

    private String permission;

    private Set<String> actions;
}
