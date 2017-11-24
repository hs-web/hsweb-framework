package org.hswebframework.web.socket.authorize;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenHolder;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.socket.CommandRequest;
import org.hswebframework.web.socket.message.WebSocketMessage;
import org.hswebframework.web.socket.processor.AbstractCommandProcessor;

public class AuthorizeCommandProcessor extends AbstractCommandProcessor {

    private UserTokenManager userTokenManager;

    public AuthorizeCommandProcessor(UserTokenManager userTokenManager) {
        this.userTokenManager = userTokenManager;
    }

    public void setUserTokenManager(UserTokenManager userTokenManager) {
        this.userTokenManager = userTokenManager;
    }

    @Override
    public String getName() {
        return "authorize";
    }

    @Override
    public void execute(CommandRequest command) {
        String accessToken = (String) command.getParameters().get("access_token");
        boolean success = false;

        if (null != accessToken) {
            UserToken token = userTokenManager.getByToken(accessToken);
            if (token != null) {
                UserTokenHolder.setCurrent(token);
                success = Authentication.current().orElse(null) != null;
                if (success) {
                    command.getSession().getAttributes().put("user_token", accessToken);
                }
            }
            sendMessage(command.getSession(), new WebSocketMessage(200, token == null ? "token not exists":"", success));
        }
    }
}
