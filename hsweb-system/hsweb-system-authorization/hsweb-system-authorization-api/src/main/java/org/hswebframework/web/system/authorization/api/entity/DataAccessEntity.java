package org.hswebframework.web.system.authorization.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class DataAccessEntity {

    private String action;

    private String type;

    private String describe;

    private Map<String,Object> config;

    public Map<String,Object> toMap(){
        Map<String,Object> map = new HashMap<>();
        map.put("type",type);
        map.put("action",action);
        map.put("describe",describe);

        if(config!=null){
            map.putAll(config);
        }

        return map;
    }
}
