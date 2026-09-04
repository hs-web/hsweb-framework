package org.hswebframework.web.oauth2.server.authentication;

import java.util.Arrays;
import java.util.Map;

/**
 * OAuth2 client authentication request backed by a shared secret.
 *
 * <p>The secret is defensively copied on construction and access. The token endpoint erases the
 * retained copy when client authentication terminates; providers must erase every copy obtained
 * from {@link #getClientSecret()} immediately after verification.</p>
 *
 * @author zhouhao
 * @since 5.0.2
 * @see DefaultOAuth2ClientSecretAuthenticationProvider
 */
public final class OAuth2ClientSecretAuthenticationRequest extends OAuth2ClientAuthenticationRequest {

    private char[] clientSecret;

    public OAuth2ClientSecretAuthenticationRequest(String clientId,
                                                   String authenticationMethod,
                                                   char[] clientSecret,
                                                   String grantType,
                                                   Map<String, String> parameters) {
        super(clientId, authenticationMethod, grantType, parameters);
        this.clientSecret = clientSecret == null ? null : clientSecret.clone();
    }

    /**
     * Obtain a defensive copy of the secret for immediate verification.
     *
     * @return a caller-owned copy, or {@code null} after cleanup or when no secret was supplied
     */
    public synchronized char[] getClientSecret() {
        return clientSecret == null ? null : clientSecret.clone();
    }

    @Override
    public synchronized void eraseCredentials() {
        if (clientSecret != null) {
            Arrays.fill(clientSecret, '\0');
            clientSecret = null;
        }
    }
}
