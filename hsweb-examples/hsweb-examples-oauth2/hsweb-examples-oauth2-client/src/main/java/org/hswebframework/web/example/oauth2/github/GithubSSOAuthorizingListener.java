package org.hswebframework.web.example.oauth2.github;

import org.hswebframework.web.WebUtil;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.basic.web.UserTokenGenerator;
import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestService;
import org.hswebframework.web.authorization.oauth2.client.listener.OAuth2CodeAuthBeforeEvent;
import org.hswebframework.web.authorization.oauth2.client.listener.OAuth2Listener;
import org.hswebframework.web.authorization.simple.SimplePermission;
import org.hswebframework.web.authorization.simple.SimpleRole;
import org.hswebframework.web.authorization.simple.SimpleUser;
import org.hswebframework.web.authorization.simple.builder.SimpleAuthenticationBuilder;
import org.hswebframework.web.authorization.simple.builder.SimpleDataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.example.oauth2.MemoryAuthenticationManager;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public class GithubSSOAuthorizingListener
        implements OAuth2Listener<OAuth2CodeAuthBeforeEvent> {


    private OAuth2RequestService oAuth2RequestService;

    private UserTokenManager userTokenManager;

    private String userCenterServerId;

    private String userAuthInfoApi = "/user";

    public GithubSSOAuthorizingListener(OAuth2RequestService oAuth2RequestService, String userCenterServerId, UserTokenManager userTokenManager) {
        this.oAuth2RequestService = oAuth2RequestService;
        this.userCenterServerId = userCenterServerId;
        this.userTokenManager = userTokenManager;
    }

    @Override
    @SuppressWarnings("all")
    public void on(OAuth2CodeAuthBeforeEvent event) {
        String code = event.getCode();

        Map<String, Object> userInfo = oAuth2RequestService
                .create(userCenterServerId)
                .byAuthorizationCode(code)
                .authorize()
                .request(userAuthInfoApi)
                .get()
                .as(Map.class);

        String name = String.valueOf(userInfo.get("name"));
        String id = String.valueOf(userInfo.get("id"));
        String bio = String.valueOf(userInfo.get("bio"));

        Authentication authentication = new SimpleAuthenticationBuilder(new SimpleDataAccessConfigBuilderFactory())
                .user(SimpleUser.builder().username(bio).name(name)
                        .type("github").id("github-user:" + id).build())
                .role(Arrays.asList(SimpleRole.builder().id("admin").name("github用户").build()))
                .permission(Arrays.asList(SimplePermission.builder().id("user-info").actions(new HashSet<>(Arrays.asList("get"))).build()))
                .attributes((Map) userInfo)
                .build();

        MemoryAuthenticationManager.addAuthentication(authentication);

        HttpSession session = WebUtil.getHttpServletRequest().getSession();

        userTokenManager.signIn(session.getId(), UserTokenGenerator.TOKEN_TYPE_SESSION_ID, authentication.getUser().getId(), -1);


    }
}
