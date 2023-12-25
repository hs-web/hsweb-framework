package org.hswebframework.web.system.authorization.api.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;
import org.hswebframework.web.api.crud.entity.GenericEntity;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.validator.CreateGroup;

import javax.persistence.Column;
import javax.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@Table(name = "s_dimension_type")
@Comment("维度类型")
public class DimensionTypeEntity extends GenericEntity<String> implements DimensionType {


    @Comment("维度类型名称")
    @Column(length = 32, nullable = false)
    @NotBlank(message = "名称不能为空", groups = CreateGroup.class)
    @Schema(description = "类型名称")
    private String name;

    @Comment("维度类型描述")
    @Column(length = 256)
    @Schema(description = "说明")
    private String describe;

}
