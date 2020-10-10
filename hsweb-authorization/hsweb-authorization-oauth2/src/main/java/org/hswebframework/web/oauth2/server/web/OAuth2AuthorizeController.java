package org.hswebframework.web.oauth2.server.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.OAuth2Exception;
import org.hswebframework.web.oauth2.server.AccessToken;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.hswebframework.web.oauth2.server.OAuth2ClientManager;
import org.hswebframework.web.oauth2.server.OAuth2GrantService;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeRequest;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeTokenRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/oauth2")
@AllArgsConstructor
@Tag(name = "OAuth2认证")
public class OAuth2AuthorizeController {

    private final OAuth2GrantService oAuth2GrantService;

    private final OAuth2ClientManager clientManager;


    @GetMapping(value = "/authorize", params = "response_type=code")
    @Operation(summary = "申请授权码,并获取重定向地址", parameters = {
            @Parameter(name = "client_id", required = true),
            @Parameter(name = "redirect_uri", required = true),
            @Parameter(name = "state"),
            @Parameter(name = "response_type", description = "固定值为code")
    })
    public Mono<String> authorizeByCode(ServerWebExchange exchange) {
        Map<String, Object> param = new HashMap<>(exchange.getRequest().getQueryParams().toSingleValueMap());

        return Authentication
                .currentReactive()
                .switchIfEmpty(Mono.error(UnAuthorizedException::new))
                .flatMap(auth -> this
                        .getOAuth2Client((String) param.get("client_id"))
                        .switchIfEmpty(Mono.error(() -> new OAuth2Exception(ErrorType.ILLEGAL_CLIENT_ID)))
                        .flatMap(client -> {
                            String redirectUri = (String) param.getOrDefault("redirect_uri", client.getRedirectUrl());
                            client.validateRedirectUri(redirectUri);
                            return oAuth2GrantService
                                    .authorizationCode()
                                    .requestCode(new AuthorizationCodeRequest(client, auth, param))
                                    .doOnNext(response -> {
                                        Optional.ofNullable(param.get("state")).ifPresent(state -> response.with("state", state));
                                    })
                                    .map(response -> buildRedirect(redirectUri, response.getParameters()));
                        }));
    }

    @GetMapping(value = "/token", params = "grant_type=authorization_code")
    @Operation(summary = "使用授权码申请token", parameters = {
            @Parameter(name = "client_id"),
            @Parameter(name = "client_secret"),
            @Parameter(name = "code"),
            @Parameter(name = "grant_type", description = "固定值为authorization_code")
    })
    @Authorize(ignore = true)
    public Mono<ResponseEntity<AccessToken>> requestTokenByCode(ServerWebExchange exchange) {
        Map<String, String> params = exchange.getRequest().getQueryParams().toSingleValueMap();

        return doRequestCode(new HashMap<>(params))
                .map(ResponseEntity::ok);
    }

    private Mono<AccessToken> doRequestCode(Map<String, Object> param) {
        return this
                .getOAuth2Client((String) param.get("client_id"))
                .switchIfEmpty(Mono.error(() -> new OAuth2Exception(ErrorType.ILLEGAL_CLIENT_ID)))
                .flatMap(client -> oAuth2GrantService
                        .authorizationCode()
                        .requestToken(new AuthorizationCodeTokenRequest(client, param)));
    }


    @SneakyThrows
    public static String urlEncode(String url) {
        return URLEncoder.encode(url, "utf-8");
    }

    static String buildRedirect(String redirectUri, Map<String, Object> params) {
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
