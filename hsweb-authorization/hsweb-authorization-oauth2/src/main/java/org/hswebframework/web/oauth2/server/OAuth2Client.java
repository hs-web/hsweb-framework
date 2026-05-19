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

@Getter
@Setter
public class OAuth2Client {

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

    public void validateRedirectUri(String redirectUri) {
        if (!isValidRedirectUri(redirectUri)) {
            throw new OAuth2Exception(ErrorType.ILLEGAL_REDIRECT_URI);
        }
    }

    private boolean isValidRedirectUri(String redirectUri) {
        if (!StringUtils.hasText(redirectUri) || !StringUtils.hasText(this.redirectUrl)) {
            return false;
        }
        URI registered = parseUri(this.redirectUrl);
        URI actual = parseUri(redirectUri);
        if (registered == null || actual == null) {
            return false;
        }
        if (registered.isOpaque() || actual.isOpaque()) {
            return registered.equals(actual);
        }
        registered = registered.normalize();
        actual = actual.normalize();
        return equalsIgnoreCase(registered.getScheme(), actual.getScheme())
                && Objects.equals(registered.getUserInfo(), actual.getUserInfo())
                && equalsIgnoreCase(registered.getHost(), actual.getHost())
                && registered.getPort() == actual.getPort()
                && Objects.equals(registered.getRawFragment(), actual.getRawFragment())
                && matchPath(registered.getPath(), actual.getPath())
                && matchQuery(registered.getRawQuery(), actual.getRawQuery());
    }

    private URI parseUri(String value) {
        try {
            return new URI(value.trim());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private boolean matchPath(String registeredPath, String actualPath) {
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

    private boolean matchQuery(String registeredQuery, String actualQuery) {
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
