package org.hswebframework.web.system.authorization.api.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.api.crud.entity.GenericTreeSortSupportEntity;
import org.hswebframework.web.validator.CreateGroup;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.sql.JDBCType;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Table(name = "s_dimension",indexes = {
        @Index(name = "idx_dims_path",columnList = "path")
})
public class DimensionEntity extends GenericTreeSortSupportEntity<String> {

    @Comment("维度类型ID")
    @Column(length = 32,name = "type_id")
    @Schema(description = "维度类型ID")
    private String typeId;

    @Comment("维度名称")
    @Column(length = 32)
    @Schema(description = "维度名称")
    @NotBlank(message = "名称不能为空",groups = CreateGroup.class)
    private String name;

    @Comment("描述")
    @Column(length = 256)
    @Schema(description = "说明")
    private String describe;

    @Column
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    @Comment("其他配置")
    @JsonCodec
    @Schema(description = "其他配置")
    private Map<String,Object> properties;

    @Schema(description = "子节点")
    private List<DimensionEntity> children;
}
