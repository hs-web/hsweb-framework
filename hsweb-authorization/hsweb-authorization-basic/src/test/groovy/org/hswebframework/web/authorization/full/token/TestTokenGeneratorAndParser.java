package org.hswebframework.web.authorization.full.token;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.basic.web.GeneratedToken;
import org.hswebframework.web.authorization.basic.web.ParsedToken;
import org.hswebframework.web.authorization.basic.web.UserTokenGenerator;
import org.hswebframework.web.authorization.basic.web.UserTokenParser;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhouhao
 * @since 3.0.2
 */
@Component
public class TestTokenGeneratorAndParser implements UserTokenGenerator, UserTokenParser {
    @Override
    public String getSupportTokenType() {
        return "test-token";
    }

    @Override
    public GeneratedToken generate(Authentication authentication) {
        String token = IDGenerator.MD5.generate();
        return new GeneratedToken() {
            @Override
            public Map<String, Object> getResponse() {
                return Collections.singletonMap("token", token);
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
            public int getTimeout() {
                return -1;
            }
        };
    }

    @Override
    public ParsedToken parseToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("token"))
                .map(token -> new ParsedToken() {
                    @Override
                    public String getToken() {
                        return token;
                    }

                    @Override
                    public String getType() {
                        return getSupportTokenType();
                    }
                }).orElse(null);
    }
}
