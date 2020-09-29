package org.hswebframework.web.oauth2.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
public class OAuth2Request {

    private Map<String, Object> parameters;


    public Optional<Object> getParameter(String key) {
        return Optional.ofNullable(parameters)
                .map(params -> params.get(key));
    }

    public OAuth2Request with(String parameter, Object key) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        parameters.put(parameter, key);
        return this;
    }
}
