package org.hswebframework.web.system.authorization.api.entity;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;
import org.hswebframework.ezorm.rdb.mapping.annotation.EnumCodec;
import org.hswebframework.web.api.crud.entity.GenericEntity;
import org.hswebframework.web.dict.EnumDict;
import org.hswebframework.web.system.authorization.api.enums.DimensionUserFeature;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;
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
    private String dimensionTypeId;

    @Comment("维度ID")
    @Column(name = "dimension_id", nullable = false, length = 32)
    private String dimensionId;

    @Comment("维度名称")
    @Column(name = "dimension_name", nullable = false)
    private String dimensionName;

    @Comment("用户ID")
    @Column(name = "user_id", nullable = false, length = 32)
    private String userId;

    @Comment("用户ID")
    @Column(name = "user_name", nullable = false)
    private String userName;

    @Comment("关系")
    @Column(length = 32)
    private String relation;

    @Column(name = "relation_name")
    @Comment("关系名称")
    private String relationName;

    @Column(name = "features")
    @ColumnType(jdbcType = JDBCType.NUMERIC, javaType = Long.class)
    @EnumCodec(toMask = true)
    private DimensionUserFeature[] features;

    public boolean hasFeature(DimensionUserFeature feature) {
        return features != null && EnumDict.in(feature, features);
    }
}
