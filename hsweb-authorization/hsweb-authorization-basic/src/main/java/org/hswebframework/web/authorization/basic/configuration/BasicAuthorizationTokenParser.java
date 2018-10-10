package org.hswebframework.web.authorization.basic.configuration;

import org.apache.commons.codec.binary.Base64;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.basic.web.AuthorizedToken;
import org.hswebframework.web.authorization.basic.web.ParsedToken;
import org.hswebframework.web.authorization.basic.web.UserTokenForTypeParser;
import org.hswebframework.web.authorization.simple.PlainTextUsernamePasswordAuthenticationRequest;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;

import javax.servlet.http.HttpServletRequest;

public class BasicAuthorizationTokenParser implements UserTokenForTypeParser {

    private AuthenticationManager authenticationManager;

    private UserTokenManager userTokenManager;

    @Override
    public String getTokenType() {
        return "basic";
    }

    public BasicAuthorizationTokenParser(AuthenticationManager authenticationManager, UserTokenManager userTokenManager) {
        this.authenticationManager = authenticationManager;
        this.userTokenManager = userTokenManager;
    }

    @Override
    public ParsedToken parseToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            return null;
        }
        if (authorization.contains(" ")) {
            String[] info = authorization.split("[ ]");
            if (info[0].equalsIgnoreCase(getTokenType())) {
                authorization = info[1];
            }
        }
        try {
            String usernameAndPassword = new String(Base64.decodeBase64(authorization));
            UserToken token = userTokenManager.getByToken(usernameAndPassword);
            if (token != null && token.isNormal()) {
                return new ParsedToken() {
                    @Override
                    public String getToken() {
                        return usernameAndPassword;
                    }

                    @Override
                    public String getType() {
                        return getTokenType();
                    }
                };
            }
            if (usernameAndPassword.contains(":")) {
                String[] arr = usernameAndPassword.split("[:]");
                Authentication authentication = authenticationManager.authenticate(new PlainTextUsernamePasswordAuthenticationRequest(arr[0], arr[1]));
                if (authentication != null) {
                    return new AuthorizedToken() {
                        @Override
                        public String getUserId() {
                            return authentication.getUser().getId();
                        }

                        @Override
                        public String getToken() {
                            return usernameAndPassword;
                        }

                        @Override
                        public String getType() {
                            return getTokenType();
                        }

                        @Override
                        public long getMaxInactiveInterval() {
                            //60分钟有效期
                            return 60 * 60 * 1000L;
                        }
                    };
                }
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }
}
