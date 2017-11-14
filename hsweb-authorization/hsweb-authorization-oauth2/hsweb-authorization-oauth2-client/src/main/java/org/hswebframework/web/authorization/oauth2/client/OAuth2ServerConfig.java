package org.hswebframework.web.authorization.oauth2.client;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since
 */
public class OAuth2ServerConfig implements Serializable{

    private String id;
    //服务名称
    private String name;
    //api根地址
    private String apiBaseUrl;
    //认证地址
    private String authUrl;
    //token获取地址
    private String accessTokenUrl;
    //客户端id
    private String clientId;
    //客户端密钥
    private String clientSecret;
    //是否启用
    private Byte   status;
    //重定向地址
    private String redirectUri;
    //服务提供商
    private String provider;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    /**
     * @return 服务名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置 服务名称
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @return api根地址
     */
    public String getApiBaseUrl() {
        return this.apiBaseUrl;
    }

    /**
     * 设置 api根地址
     */
    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    /**
     * @return 认证地址
     */
    public String getAuthUrl() {
        return this.authUrl;
    }

    /**
     * 设置 认证地址
     */
    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    /**
     * @return token获取地址
     */
    public String getAccessTokenUrl() {
        return this.accessTokenUrl;
    }

    /**
     * 设置 token获取地址
     */
    public void setAccessTokenUrl(String accessTokenUrl) {
        this.accessTokenUrl = accessTokenUrl;
    }

    /**
     * @return 客户端id
     */
    public String getClientId() {
        return this.clientId;
    }

    /**
     * 设置 客户端id
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * @return 客户端密钥
     */
    public String getClientSecret() {
        return this.clientSecret;
    }

    /**
     * 设置 客户端密钥
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * @return 是否启用
     */
    public Byte getStatus() {
        return this.status;
    }

    /**
     * 设置 是否启用
     */
    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
