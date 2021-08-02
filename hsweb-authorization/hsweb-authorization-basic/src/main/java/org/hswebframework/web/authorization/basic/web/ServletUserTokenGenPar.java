package org.hswebframework.web.authorization.basic.web;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.token.ParsedToken;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class ServletUserTokenGenPar implements UserTokenParser, UserTokenGenerator {
    private long timeout = TimeUnit.MINUTES.toMillis(30);

    private String headerName = "X-Access-Token";

    @Override
    public String getSupportTokenType() {
        return "default";
    }


    @Override
    public GeneratedToken generate(Authentication authentication) {
        String token = IDGenerator.MD5.generate();

        return new GeneratedToken() {
            @Override
            public Map<String, Object> getResponse() {
                return Collections.singletonMap("expires", timeout);
            }

            @Override
            public String getToken() {
                return token;
            }

            @Override
            public String getType() {
                return getSupportTokenType();
            }

            @Override
            public long getTimeout() {
                return timeout;
            }
        };
    }

    @Override
    public ParsedToken parseToken(HttpServletRequest request) {
        String token = Optional
                .ofNullable(request.getHeader(headerName))
                .orElseGet(() -> request.getParameter(":X_Access_Token"));
        if (StringUtils.hasText(token)) {
            return ParsedToken.of(getSupportTokenType(), token);
        }
        return null;
    }
}
