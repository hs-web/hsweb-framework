package org.hswebframework.web.oauth2.server.authentication;

import lombok.Getter;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Successfully authenticated OAuth2 client context passed to grant processing.
 *
 * <p>The context creates a defensive client projection without {@code clientSecret}, retains the
 * verified client authentication method when available, and copies attributes into an immutable
 * map with {@code client_secret} removed. Authenticators should add only the minimum non-secret
 * metadata required by the selected grant handler.</p>
 *
 * @author zhouhao
 * @since 5.0.2
 * @see ReactiveOAuth2ClientAuthenticator
 * @see org.hswebframework.web.oauth2.server.credential.ClientCredentialGrantHandler
 */
@Getter
public class OAuth2ClientAuthentication {

    private final OAuth2Client client;

    private final String clientType;

    private final String authenticationMethod;

    private final Map<String, Object> attributes;

    /**
     * Create a context using the trusted type configured on the client.
     *
     * @param client authenticated source client; copied into a secret-free projection
     */
    public OAuth2ClientAuthentication(OAuth2Client client) {
        this(client,
             Objects.requireNonNull(client, "client must not be null").getClientType(),
             null,
             Collections.emptyMap());
    }

    /**
     * Create a typed authenticated client context.
     *
     * @param client authenticated source client; copied into a secret-free projection
     * @param clientType stable, non-empty type used for exact grant-handler routing
     * @param attributes optional authentication metadata; defensively copied, made immutable, and
     *                   stripped of {@code client_secret}
     */
    public OAuth2ClientAuthentication(OAuth2Client client,
                                      String clientType,
                                      Map<String, Object> attributes) {
        this(client, clientType, null, attributes);
    }

    /**
     * Create an authenticated client context with the verified authentication method.
     *
     * @param client authenticated source client; copied into a secret-free projection
     * @param clientType stable, non-empty type used for exact grant-handler routing
     * @param authenticationMethod verified OAuth2 client authentication method, or {@code null}
     *                             for legacy programmatic callers without method information
     * @param attributes optional authentication metadata; defensively copied, made immutable, and
     *                   stripped of {@code client_secret}
     */
    public OAuth2ClientAuthentication(OAuth2Client client,
                                      String clientType,
                                      String authenticationMethod,
                                      Map<String, Object> attributes) {
        OAuth2Client source = Objects.requireNonNull(client, "client must not be null");
        if (!StringUtils.hasText(clientType)) {
            throw new IllegalArgumentException("clientType must not be empty");
        }
        if (authenticationMethod != null && !StringUtils.hasText(authenticationMethod)) {
            throw new IllegalArgumentException("authenticationMethod must not be blank");
        }
        this.clientType = clientType;
        this.authenticationMethod = authenticationMethod;
        this.client = createClientView(source, clientType);
        if (attributes == null || attributes.isEmpty()) {
            this.attributes = Collections.emptyMap();
        } else {
            Map<String, Object> safeAttributes = new LinkedHashMap<>(attributes);
            safeAttributes.remove("client_secret");
            this.attributes = Collections.unmodifiableMap(safeAttributes);
        }
    }

    private static OAuth2Client createClientView(OAuth2Client source, String clientType) {
        OAuth2Client client = new OAuth2Client();
        client.setClientId(source.getClientId());
        client.setName(source.getName());
        client.setDescription(source.getDescription());
        client.setRedirectUrl(source.getRedirectUrl());
        client.setUserId(source.getUserId());
        client.setClientType(clientType);
        return client;
    }
}
