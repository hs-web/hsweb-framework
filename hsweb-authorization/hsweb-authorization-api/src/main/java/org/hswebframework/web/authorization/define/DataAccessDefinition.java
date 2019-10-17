package org.hswebframework.web.authorization.define;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class DataAccessDefinition {

    List<DataAccessTypeDefinition> dataAccessTypes=new ArrayList<>();

    public Optional<DataAccessTypeDefinition> getType(String typeId){
        return dataAccessTypes
                .stream()
                .filter(datd->datd.getId().equalsIgnoreCase(typeId))
                .findAny();
    }
}
