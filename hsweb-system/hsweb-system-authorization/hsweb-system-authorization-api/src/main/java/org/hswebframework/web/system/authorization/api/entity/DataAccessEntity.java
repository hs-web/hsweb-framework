package org.hswebframework.web.system.authorization.api.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class DataAccessEntity {

    @Schema(description = "操作标识")
    private String action;

    @Schema(description = "数据权限类型")
    private String type;

    @Schema(description = "说明")
    private String describe;

    @Schema(description = "配置")
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
