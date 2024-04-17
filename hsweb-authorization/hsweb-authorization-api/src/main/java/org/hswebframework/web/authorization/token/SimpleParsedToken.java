package org.hswebframework.web.authorization.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;

import java.util.function.BiConsumer;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
public class SimpleParsedToken implements ParsedToken {

    private String type;

    private String token;

    private BiConsumer<HttpHeaders,String> headerSetter;

    @Override
    public void apply(HttpHeaders headers) {
        if (headerSetter != null) {
            headerSetter.accept(headers,token);
        }
    }
}
