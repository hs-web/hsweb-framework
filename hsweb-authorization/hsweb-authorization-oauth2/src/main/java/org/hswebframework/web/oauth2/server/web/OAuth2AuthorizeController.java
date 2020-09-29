package org.hswebframework.web.oauth2.server.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.authorization.token.TokenAuthenticationManager;
import org.hswebframework.web.oauth2.server.*;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeRequest;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeTokenRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/oauth2")
@AllArgsConstructor
public class OAuth2AuthorizeController {

    private final OAuth2GrantService oAuth2GrantService;

    private final OAuth2ClientManager clientManager;

    @PostMapping(value = "/authorize", params = "response_type=code")
    @Operation(summary = "申请授权码,并获取重定向地址", parameters = {
            @Parameter(description = "client_id"),
            @Parameter(description = "redirect_uri"),
            @Parameter(description = "state")
    })
    public Mono<String> authorizeByCode(@RequestBody Mono<Map<String, Object>> params) {

        return Authentication
                .currentReactive()
                .switchIfEmpty(Mono.error(UnAuthorizedException::new))
                .flatMap(auth -> params
                        .flatMap(param -> this
                                .getOAuth2Client((String) param.get("client_id"))
                                .flatMap(client -> {
                                    String redirectUri = (String) param.getOrDefault("redirect_uri", client.getRedirectUrl());
                                    client.validateRedirectUri(redirectUri);
                                    return oAuth2GrantService
                                            .code()
                                            .requestCode(new AuthorizationCodeRequest(client, auth, param))
                                            .map(authorizationCodeResponse -> buildRedirect(redirectUri, authorizationCodeResponse.getParameters()));
                                })));
    }

    @PostMapping(value = "/token", params = "grant_type=authorization_code")
    @Operation(summary = "使用授权码申请token",parameters = {
            @Parameter(description = "client_id"),
            @Parameter(description = "client_secret"),
            @Parameter(description = "code")
    })
    @Authorize(ignore = true)
    public Mono<ResponseEntity<AccessToken>> requestTokenByCode(@RequestBody Mono<Map<String, Object>> params) {

        return params
                .flatMap(param -> this
                        .getOAuth2Client((String) param.get("client_id"))
                        .flatMap(client -> oAuth2GrantService
                                .code()
                                .requestToken(new AuthorizationCodeTokenRequest(client, param))))
                .map(ResponseEntity::ok);
    }


    @SneakyThrows
    public static String urlEncode(String url) {
        return URLEncoder.encode(url, "utf-8");
    }

    public String buildRedirect(String redirectUri, Map<String, Object> params) {
        String paramsString = params.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + urlEncode(String.valueOf(e.getValue())))
                .collect(Collectors.joining("&"));
        if (redirectUri.contains("?")) {
            return redirectUri + "&" + paramsString;
        }
        return redirectUri + "?" + paramsString;
    }


    private Mono<OAuth2Client> getOAuth2Client(String id) {
        return clientManager.getClient(id);
    }
}
