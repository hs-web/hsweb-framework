package org.hswebframework.web.authorization.define;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class DataAccessDefinition {

    Set<DataAccessTypeDefinition> dataAccessTypes = new HashSet<>();

    public Optional<DataAccessTypeDefinition> getType(String typeId) {
        return dataAccessTypes
                .stream()
                .filter(type -> type.getId() != null && type.getId().equalsIgnoreCase(typeId))
                .findAny();
    }
}
