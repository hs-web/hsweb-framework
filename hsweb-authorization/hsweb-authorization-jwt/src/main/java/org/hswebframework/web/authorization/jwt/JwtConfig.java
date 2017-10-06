package org.hswebframework.web.authorization.jwt;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * jwt
 */
public class JwtConfig {

    private String id = "hsweb-jwt";

    private String secret = Base64.encodeBase64String("hsweb.jwt.secret".getBytes());

    private int ttl = 60 * 60 * 1000;

    private int refreshTtl = 12 * 60 * 60 * 1000;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public int getRefreshTtl() {
        return refreshTtl;
    }

    public void setRefreshTtl(int refreshTtl) {
        this.refreshTtl = refreshTtl;
    }

    public SecretKey generalKey() {
        byte[] encodedKey = Base64.decodeBase64(secret);
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
