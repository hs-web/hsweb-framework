package org.hswebframework.web.oauth2.server;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.OAuth2Exception;
import org.springframework.util.StringUtils;

import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * OAuth2 client configuration loaded from a trusted client registry.
 *
 * <p>{@link #getClientType()} exposes server-managed routing metadata. The token endpoint does not
 * derive it from request parameters, headers, or form data.</p>
 *
 * @author zhouhao
 */
@Getter
@Setter
public class OAuth2Client {

    /**
     * Default routing type for legacy clients without an explicit server-side classification.
     *
     * @since 5.0.2
     */
    public static final String DEFAULT_CLIENT_TYPE = "default";

    @NotBlank
    private String clientId;

    @NotBlank
    private String clientSecret;

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String redirectUrl;

    //client 所属用户
    private String userId;

    private String clientType = DEFAULT_CLIENT_TYPE;

    /**
     * Get the trusted client type used for grant-handler routing.
     *
     * @return configured type, or {@link #DEFAULT_CLIENT_TYPE} for legacy null or blank values
     */
    public String getClientType() {
        return StringUtils.hasText(clientType) ? clientType : DEFAULT_CLIENT_TYPE;
    }

    public void validateRedirectUri(String redirectUri) {
        validateRedirectUri(redirectUri, OAuth2Properties.RedirectUriValidationMode.COMPATIBLE);
    }

    public void validateRedirectUri(String redirectUri, OAuth2Properties.RedirectUriValidationMode validationMode) {
        if (!isValidRedirectUri(redirectUri, validationMode)) {
            throw new OAuth2Exception(ErrorType.ILLEGAL_REDIRECT_URI);
        }
    }

    public boolean isSameRedirectUri(String redirectUri, String anotherRedirectUri) {
        URI left = parseUri(redirectUri);
        URI right = parseUri(anotherRedirectUri);
        return left != null
                && right != null
                && !hasFragment(left)
                && !hasFragment(right)
                && isExactMatch(left, right);
    }

    private boolean isValidRedirectUri(String redirectUri, OAuth2Properties.RedirectUriValidationMode validationMode) {
        if (!StringUtils.hasText(redirectUri) || !StringUtils.hasText(this.redirectUrl)) {
            return false;
        }
        URI registered = parseUri(this.redirectUrl);
        URI actual = parseUri(redirectUri);
        if (registered == null || actual == null) {
            return false;
        }
        if (hasFragment(registered) || hasFragment(actual)) {
            return false;
        }
        registered = registered.normalize();
        actual = actual.normalize();
        if (registered.isOpaque() || actual.isOpaque()) {
            return isExactMatch(registered, actual);
        }
        if (!hasSameEndpoint(registered, actual)) {
            return false;
        }
        if (validationMode == OAuth2Properties.RedirectUriValidationMode.EXACT) {
            return isExactPathAndQuery(registered, actual);
        }
        return matchCompatiblePath(registered.getPath(), actual.getPath())
                && matchCompatibleQuery(registered.getRawQuery(), actual.getRawQuery());
    }

    private boolean hasSameEndpoint(URI registered, URI actual) {
        return equalsIgnoreCase(registered.getScheme(), actual.getScheme())
                && Objects.equals(registered.getUserInfo(), actual.getUserInfo())
                && equalsIgnoreCase(registered.getHost(), actual.getHost())
                && registered.getPort() == actual.getPort();
    }

    private boolean isExactMatch(URI left, URI right) {
        if (!equalsIgnoreCase(left.getScheme(), right.getScheme())) {
            return false;
        }
        if (left.isOpaque() || right.isOpaque()) {
            return Objects.equals(left.getRawSchemeSpecificPart(), right.getRawSchemeSpecificPart());
        }
        return Objects.equals(left.getUserInfo(), right.getUserInfo())
                && equalsIgnoreCase(left.getHost(), right.getHost())
                && left.getPort() == right.getPort()
                && isExactPathAndQuery(left, right)
                && Objects.equals(left.getRawFragment(), right.getRawFragment());
    }

    private boolean isExactPathAndQuery(URI registered, URI actual) {
        return Objects.equals(pathOrEmpty(registered), pathOrEmpty(actual))
                && Objects.equals(registered.getRawQuery(), actual.getRawQuery());
    }

    private URI parseUri(String value) {
        try {
            return new URI(value.trim());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private boolean hasFragment(URI uri) {
        return StringUtils.hasLength(uri.getRawFragment());
    }

    private String pathOrEmpty(URI uri) {
        return uri.getPath() == null ? "" : uri.getPath();
    }

    private boolean matchCompatiblePath(String registeredPath, String actualPath) {
        String registered = registeredPath == null ? "" : registeredPath;
        String actual = actualPath == null ? "" : actualPath;
        if (registered.isEmpty()) {
            return actual.isEmpty() || actual.startsWith("/");
        }
        if (actual.equals(registered)) {
            return true;
        }
        if (registered.endsWith("/")) {
            return actual.startsWith(registered);
        }
        return actual.startsWith(registered + "/");
    }

    private boolean matchCompatibleQuery(String registeredQuery, String actualQuery) {
        if (!StringUtils.hasLength(registeredQuery)) {
            return true;
        }
        if (!StringUtils.hasLength(actualQuery)) {
            return false;
        }
        return actualQuery.equals(registeredQuery)
                || actualQuery.startsWith(registeredQuery + "&");
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return left == null ? right == null : left.equalsIgnoreCase(right);
    }

    public void validateSecret(String secret) {
        if (!StringUtils.hasLength(secret) || (!secret.equals(this.clientSecret))) {
            throw new OAuth2Exception(ErrorType.ILLEGAL_CLIENT_SECRET);
        }
    }

}
