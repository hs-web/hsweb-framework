package org.hswebframework.web.oauth2.server;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class OAuth2Response implements Serializable {
    @Hidden
    private Map<String,Object> parameters;

    public OAuth2Response with(String parameter, Object key) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        parameters.put(parameter, key);
        return this;
    }
}
