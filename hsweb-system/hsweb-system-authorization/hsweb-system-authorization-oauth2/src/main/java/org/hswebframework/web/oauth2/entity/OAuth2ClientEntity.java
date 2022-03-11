package org.hswebframework.web.oauth2.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;
import org.hswebframework.ezorm.rdb.mapping.annotation.DefaultValue;
import org.hswebframework.ezorm.rdb.mapping.annotation.EnumCodec;
import org.hswebframework.web.api.crud.entity.GenericEntity;
import org.hswebframework.web.bean.ToString;
import org.hswebframework.web.crud.generator.Generators;
import org.hswebframework.web.oauth2.enums.OAuth2ClientState;
import org.hswebframework.web.oauth2.server.OAuth2Client;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Table(name = "s_oauth2_client")
@Comment("OAuth2客户端")
@Getter
@Setter
public class OAuth2ClientEntity extends GenericEntity<String> {

    @Column(length = 1024)
    @Schema(description = "Logo地址")
    private String logoUrl;

    @Column(length = 64, nullable = false)
    @Schema(description = "客户端名称")
    @NotBlank
    private String name;

    @Column(length = 128, nullable = false)
    @Schema(description = "密钥")
    @NotBlank
    @ToString.Ignore
    private String secret;

    @Column(length = 64, nullable = false)
    @Schema(description = "绑定用户ID")
    @NotBlank
    private String userId;

    @Column(length = 1024, nullable = false)
    @Schema(description = "回调地址")
    @NotBlank
    private String callbackUri;

    @Column(length = 1024, nullable = false)
    @Schema(description = "首页地址")
    @NotBlank
    private String homeUri;

    @Column
    @Schema(description = "说明")
    private String description;

    @Column(length = 32)
    @EnumCodec
    @ColumnType(javaType = String.class)
    @DefaultValue("enabled")
    @Schema(description = "状态")
    private OAuth2ClientState state;

    @Column(nullable = false)
    @Schema(description = "创建时间")
    @DefaultValue(generator = Generators.CURRENT_TIME)
    private Long createTime;

    public boolean enabled() {
        return state == OAuth2ClientState.enabled;
    }

    public OAuth2Client toOAuth2Client() {
        OAuth2Client client = new OAuth2Client();
        client.setClientSecret(secret);
        client.setClientId(getId());
        client.setName(getName());
        client.setRedirectUrl(callbackUri);
        client.setDescription(description);
        client.setUserId(userId);
        return client;
    }
}
