package org.hswebframework.web.oauth2.server.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.hswebframework.web.oauth2.server.OAuth2Properties;
import org.hswebframework.web.oauth2.server.authentication.ClientSecretAuthenticationRequestConverter;
import org.hswebframework.web.oauth2.server.authentication.CompositeReactiveOAuth2ClientAuthenticationRequestResolver;
import org.hswebframework.web.oauth2.server.authentication.DefaultReactiveOAuth2ClientAuthenticator;
import org.hswebframework.web.oauth2.server.authentication.OAuth2ClientAuthentication;
import org.hswebframework.web.oauth2.server.authentication.OAuth2ClientAuthenticationRequest;
import org.hswebframework.web.oauth2.server.authentication.ReactiveOAuth2ClientAuthenticator;
import org.hswebframework.web.oauth2.server.authentication.ReactiveOAuth2ClientAuthenticationRequestResolver;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeRequest;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeTokenRequest;
import org.hswebframework.web.oauth2.server.credential.ClientCredentialRequest;
import org.hswebframework.web.oauth2.server.refresh.RefreshTokenRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/oauth2")
@Tag(name = "OAuth2认证")
public class OAuth2AuthorizeController {

    private final OAuth2GrantService oAuth2GrantService;

    private final OAuth2ClientManager clientManager;

    private final OAuth2Properties properties;

    private final ReactiveOAuth2ClientAuthenticator clientAuthenticator;

    private final ReactiveOAuth2ClientAuthenticationRequestResolver clientAuthenticationRequestResolver;

    /**
     * 兼容旧的三参数构造方式，也作为组件扫描场景未提供认证器 Bean 时的回退。
     *
     * @deprecated 优先同时注入 {@link ReactiveOAuth2ClientAuthenticator} 和
     * {@link ReactiveOAuth2ClientAuthenticationRequestResolver}，使用五参数构造器。
     */
    @Deprecated
    @Autowired(required = false)
    public OAuth2AuthorizeController(OAuth2GrantService oAuth2GrantService,
                                     OAuth2ClientManager clientManager,
                                     OAuth2Properties properties) {
        this(oAuth2GrantService,
             clientManager,
             properties,
             new DefaultReactiveOAuth2ClientAuthenticator(clientManager),
             defaultClientAuthenticationRequestResolver());
    }

    /**
     * 兼容仅自定义客户端认证器的调用方，并使用内置 Secret 请求解析器。
     *
     * <p>需要扩展 HTTP 认证证据时，应使用五参数构造器同时提供请求解析器。</p>
     */
    @Autowired(required = false)
    public OAuth2AuthorizeController(OAuth2GrantService oAuth2GrantService,
                                     OAuth2ClientManager clientManager,
                                     OAuth2Properties properties,
                                     ReactiveOAuth2ClientAuthenticator clientAuthenticator) {
        this(oAuth2GrantService,
             clientManager,
             properties,
             clientAuthenticator,
             defaultClientAuthenticationRequestResolver());
    }

    /**
     * 使用已配置的 HTTP 认证证据解析器和客户端认证门面。
     *
     * <p>请求解析器只负责把 HTTP 证据转换为类型化认证请求，认证门面负责验证客户端；
     * Controller 不假设具体认证方式，也不会把原始凭证传递给 grant。</p>
     */
    @Autowired(required = false)
    public OAuth2AuthorizeController(
        OAuth2GrantService oAuth2GrantService,
        OAuth2ClientManager clientManager,
        OAuth2Properties properties,
        ReactiveOAuth2ClientAuthenticator clientAuthenticator,
        ReactiveOAuth2ClientAuthenticationRequestResolver clientAuthenticationRequestResolver) {
        this.oAuth2GrantService = oAuth2GrantService;
        this.clientManager = clientManager;
        this.properties = properties;
        this.clientAuthenticator = clientAuthenticator;
        this.clientAuthenticationRequestResolver = clientAuthenticationRequestResolver;
    }

    private static ReactiveOAuth2ClientAuthenticationRequestResolver
    defaultClientAuthenticationRequestResolver() {
        return new CompositeReactiveOAuth2ClientAuthenticationRequestResolver(
            java.util.Collections.emptyList(),
            new ClientSecretAuthenticationRequestConverter());
    }

    @GetMapping(value = "/authorize", params = "response_type=code")
    @Operation(summary = "申请授权码,并获取重定向地址", parameters = {
        @Parameter(name = "client_id", required = true),
        @Parameter(name = "redirect_uri", required = true),
        @Parameter(name = "state"),
        @Parameter(name = "response_type", description = "固定值为code")
    })
    public Mono<String> authorizeByCode(ServerWebExchange exchange) {
        Map<String, String> param = new HashMap<>(exchange.getRequest().getQueryParams().toSingleValueMap());

        return Authentication
            .currentReactive()
            .switchIfEmpty(Mono.error(UnAuthorizedException::new))
            .flatMap(auth -> this
                .getOAuth2Client(param.get("client_id"))
                .flatMap(client -> {
                    String redirectUri = param.getOrDefault("redirect_uri", client.getRedirectUrl());
                    if (redirectUri != null) {
                        redirectUri = redirectUri.trim();
                    } else {
                        redirectUri = client.getRedirectUrl();
                    }
                    client.validateRedirectUri(redirectUri, properties.getRedirectUriValidationMode());
                    final String validatedRedirectUri = redirectUri;
                    param.put("redirect_uri", validatedRedirectUri);
                    return oAuth2GrantService
                        .authorizationCode()
                        .requestCode(new AuthorizationCodeRequest(client, auth, param))
                        .doOnNext(response -> {
                            Optional
                                .ofNullable(param.get("state"))
                                .ifPresent(state -> response.with("state", state));
                        })
                        .map(response -> buildRedirect(validatedRedirectUri, response.getParameters()));
                }));
    }

    @GetMapping(value = "/token")
    @Operation(summary = "(GET)申请token", parameters = {
        @Parameter(name = "client_id"),
        @Parameter(name = "client_secret"),
        @Parameter(name = "code", description = "grantType为authorization_code时不能为空"),
        @Parameter(name = "grant_type", schema = @Schema(implementation = GrantType.class))
    })
    @Authorize(ignore = true)
    public Mono<ResponseEntity<AccessToken>> requestTokenByCode(
        @RequestParam("grant_type") GrantType grantType,
        ServerWebExchange exchange) {
        return requestToken(grantType, exchange, exchange.getRequest().getQueryParams());
    }


    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "(POST)申请token", parameters = {
        @Parameter(name = "client_id"),
        @Parameter(name = "client_secret"),
        @Parameter(name = "code", description = "grantType为authorization_code时不能为空"),
        @Parameter(name = "grant_type", schema = @Schema(implementation = GrantType.class))
    })
    @Authorize(ignore = true)
    public Mono<ResponseEntity<AccessToken>> requestTokenByCode(ServerWebExchange exchange) {
        return exchange
            .getFormData()
            .flatMap(params -> {
                GrantType grantType = GrantType.of(params.getFirst("grant_type"));
                return requestToken(grantType, exchange, params);
            });
    }

    private Mono<ResponseEntity<AccessToken>> requestToken(
        GrantType grantType,
        ServerWebExchange exchange,
        MultiValueMap<String, String> parameters) {
        return clientAuthenticationRequestResolver
            .resolve(exchange, parameters, grantType.name())
            .switchIfEmpty(Mono.error(() -> new OAuth2Exception(ErrorType.ILLEGAL_AUTHORIZATION)))
            .flatMap(request -> authenticateClient(request)
                .flatMap(authentication -> grantType.requestToken(
                    oAuth2GrantService,
                    authentication,
                    new HashMap<>(request.getParameters()))))
            .map(ResponseEntity::ok);
    }

    private Mono<OAuth2ClientAuthentication> authenticateClient(OAuth2ClientAuthenticationRequest request) {
        return Mono
            .defer(() -> clientAuthenticator.authenticate(request))
            .switchIfEmpty(Mono.error(() -> new OAuth2Exception(ErrorType.UNAUTHORIZED_CLIENT)))
            // Authentication owns the credential lifetime; grants only see the sanitized result.
            .doFinally(ignore -> request.eraseCredentials());
    }

    public enum GrantType {
        authorization_code {
            @Override
            Mono<AccessToken> requestToken(OAuth2GrantService service,
                                           OAuth2ClientAuthentication authentication,
                                           Map<String, String> param) {
                return service
                    .authorizationCode()
                    .requestToken(new AuthorizationCodeTokenRequest(authentication.getClient(), param));
            }
        },
        client_credentials {
            @Override
            Mono<AccessToken> requestToken(OAuth2GrantService service,
                                           OAuth2ClientAuthentication authentication,
                                           Map<String, String> param) {
                return service
                    .clientCredential()
                    .requestToken(new ClientCredentialRequest(authentication, param));
            }
        },
        refresh_token {
            @Override
            Mono<AccessToken> requestToken(OAuth2GrantService service,
                                           OAuth2ClientAuthentication authentication,
                                           Map<String, String> param) {
                return service
                    .refreshToken()
                    .requestToken(new RefreshTokenRequest(authentication.getClient(), param));
            }
        };

        abstract Mono<AccessToken> requestToken(OAuth2GrantService service,
                                                OAuth2ClientAuthentication authentication,
                                                Map<String, String> param);

        static GrantType of(String name) {
            try {
                return GrantType.valueOf(name);
            } catch (Throwable e) {
                throw new OAuth2Exception(ErrorType.UNSUPPORTED_GRANT_TYPE);
            }
        }
    }

    @SneakyThrows
    public static String urlEncode(String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }

    static String buildRedirect(String redirectUri, Map<String, Object> params) {
        String paramsString = params
            .entrySet()
            .stream()
            .map(e -> e.getKey() + "=" + urlEncode(String.valueOf(e.getValue())))
            .collect(Collectors.joining("&"));
        if (redirectUri.contains("?")) {
            return redirectUri + "&" + paramsString;
        }
        return redirectUri + "?" + paramsString;
    }

    private Mono<OAuth2Client> getOAuth2Client(String id) {
        return clientManager
            .getClient(id)
            .switchIfEmpty(Mono.error(() -> new OAuth2Exception(ErrorType.ILLEGAL_CLIENT_ID)));
    }
}
