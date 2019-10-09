package org.hswebframework.web.system.authorization.entity;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;
import org.hswebframework.web.crud.entity.GenericTreeSortSupportEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.List;

@Getter
@Setter
@Table(name = "s_dimension")
public class DimensionEntity extends GenericTreeSortSupportEntity<String> {

    @Comment("维度名称")
    @Column(length = 32)
    private String name;

    @Comment("描述")
    @Column(length = 256)
    private String describe;

    @Column(length = 32,name = "association_id")
    @Comment("关联维度")
    private String associationId;

    @Column(length = 32,name = "association_relation")
    @Comment("关联维度关系")
    private String associationRelation;

    @Column(length = 128,name = "association_relation_name")
    @Comment("关联维度关系名称")
    private String associationRelationName;


    private List<DimensionEntity> children;
}
