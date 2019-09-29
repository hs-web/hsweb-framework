package org.hswebframework.web.entity.organizational;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * 关系信息
 *
 * @author hsweb-generator-online
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "s_relation_info")
public class SimpleRelationInfoEntity extends SimpleGenericEntity<String> implements RelationInfoEntity {
    private static final long serialVersionUID = -7285786918328019221L;
    //关系从
    @Column(name = "relation_from",length = 32)
    private String relationFrom;
    //关系定义id
    @Column(name = "relation_id",length = 32)
    private String relationId;
    //关系至
    @Column(name = "relation_to",length = 32)
    private String relationTo;
    //关系类型从,如:人员
    @Column(name = "relation_type_from",length = 32)
    private String relationTypeFrom;
    //关系类型至,如:部门
    @Column(name = "relation_type_to",length = 32)
    private String relationTypeTo;
    //状态
    @Column
    private Byte status;
}