package org.hswebframework.web.oauth2.server.authentication;

import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.OAuth2Exception;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts {@code client_secret_basic} and {@code client_secret_post} HTTP credentials.
 *
 * <p>A Basic header retains precedence over query or form credentials. Malformed Basic input is
 * normalized to the established client id or secret errors and never falls back to form data.</p>
 *
 * @author zhouhao
 * @since 5.0.2
 */
public class ClientSecretAuthenticationRequestConverter
    implements ReactiveOAuth2ClientAuthenticationRequestConverter {

    @Override
    public Mono<OAuth2ClientAuthenticationRequest> convert(ServerWebExchange exchange,
                                                           MultiValueMap<String, String> parameters,
                                                           String grantType) {
        return Mono.fromSupplier(() -> {
            String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authorization != null && authorization.startsWith("Basic ")) {
                String[] credentials = decodeBasic(authorization);
                return createRequest(credentials[0],
                                     credentials[1],
                                     OAuth2ClientAuthenticationRequest.CLIENT_SECRET_BASIC,
                                     grantType,
                                     parameters);
            }
            assertSingleValue(parameters, "client_id", ErrorType.ILLEGAL_CLIENT_ID);
            assertSingleValue(parameters, "client_secret", ErrorType.ILLEGAL_CLIENT_SECRET);
            return createRequest(value(parameters, "client_id"),
                                 value(parameters, "client_secret"),
                                 OAuth2ClientAuthenticationRequest.CLIENT_SECRET_POST,
                                 grantType,
                                 parameters);
        });
    }

    private OAuth2ClientAuthenticationRequest createRequest(String clientId,
                                                            String secret,
                                                            String method,
                                                            String grantType,
                                                            MultiValueMap<String, String> parameters) {
        if (!StringUtils.hasText(clientId)) {
            throw new OAuth2Exception(ErrorType.ILLEGAL_CLIENT_ID);
        }
        if (secret.isEmpty()) {
            throw new OAuth2Exception(ErrorType.ILLEGAL_CLIENT_SECRET);
        }
        Map<String, String> safeParameters = new HashMap<>(parameters.toSingleValueMap());
        safeParameters.remove("client_secret");
        return new OAuth2ClientSecretAuthenticationRequest(clientId,
                                                           method,
                                                           secret.toCharArray(),
                                                           grantType,
                                                           safeParameters);
    }

    private String[] decodeBasic(String authorization) {
        try {
            byte[] decoded = java.util.Base64
                .getDecoder()
                .decode(authorization.substring("Basic ".length()));
            String credentials = StandardCharsets.UTF_8
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(decoded))
                .toString();
            int separator = credentials.indexOf(':');
            if (separator < 0) {
                return new String[]{credentials, ""};
            }
            return new String[]{credentials.substring(0, separator),
                                credentials.substring(separator + 1)};
        } catch (IllegalArgumentException | CharacterCodingException ignore) {
            throw new OAuth2Exception(ErrorType.ILLEGAL_CLIENT_ID);
        }
    }

    private void assertSingleValue(MultiValueMap<String, String> parameters,
                                   String name,
                                   ErrorType errorType) {
        List<String> values = parameters.get(name);
        if (values != null && values.size() > 1) {
            throw new OAuth2Exception(errorType);
        }
    }

    private String value(MultiValueMap<String, String> parameters, String name) {
        String value = parameters.getFirst(name);
        return value == null ? "" : value;
    }
}
