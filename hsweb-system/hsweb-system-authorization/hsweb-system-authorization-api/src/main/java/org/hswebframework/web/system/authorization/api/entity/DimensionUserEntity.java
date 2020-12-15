package org.hswebframework.web.system.authorization.api.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;
import org.hswebframework.ezorm.rdb.mapping.annotation.EnumCodec;
import org.hswebframework.web.api.crud.entity.GenericEntity;
import org.hswebframework.web.dict.EnumDict;
import org.hswebframework.web.system.authorization.api.enums.DimensionUserFeature;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.sql.JDBCType;

@Getter
@Setter
@Table(name = "s_dimension_user", indexes = {
        @Index(name = "idx_dimsu_dimension_id", columnList = "dimension_id"),
        @Index(name = "idx_dimsu_dimension_type_id", columnList = "dimension_type_id"),
        @Index(name = "idx_dimsu_user_id", columnList = "user_id"),

})
public class DimensionUserEntity extends GenericEntity<String> {

    @Comment("维度类型ID")
    @Column(name = "dimension_type_id", nullable = false, length = 32)
    @Schema(description = "维度类型ID,如: org,tenant")
    private String dimensionTypeId;

    @Comment("维度ID")
    @Column(name = "dimension_id", nullable = false, length = 32)
    @Schema(description = "维度ID")
    private String dimensionId;

    @Comment("维度名称")
    @Column(name = "dimension_name", nullable = false)
    @NotBlank(message = "[dimensionName]不能为空")
    @Schema(description = "维度名称")
    private String dimensionName;

    @Comment("用户ID")
    @Column(name = "user_id", nullable = false, length = 32)
    @Schema(description = "用户ID")
    private String userId;

    @Comment("用户名")
    @Column(name = "user_name", nullable = false)
    @Schema(description = "用户名")
    private String userName;

    @Comment("关系")
    @Column(length = 32)
    @Schema(description = "维度关系")
    private String relation;

    @Column(name = "relation_name")
    @Comment("关系名称")
    @Schema(description = "维度关系名称")
    private String relationName;

    @Column(name = "features")
    @ColumnType(jdbcType = JDBCType.NUMERIC, javaType = Long.class)
    @EnumCodec(toMask = true)
    @Schema(description = "其他功能")
    private DimensionUserFeature[] features;

    public void generateId() {
        if (StringUtils.isEmpty(getId())) {
            String id = DigestUtils
                    .md5DigestAsHex(String.format("%s-%s-%s",
                                                  dimensionTypeId,
                                                  dimensionId, userId).getBytes());
            setId(id);
        }
    }

    public boolean hasFeature(DimensionUserFeature feature) {
        return features != null && EnumDict.in(feature, features);
    }
}
