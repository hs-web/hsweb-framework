package org.hswebframework.web.entity.organizational;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * 关系定义
 *
 * @author hsweb-generator-online
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "s_relation_def")
public class SimpleRelationDefineEntity extends SimpleGenericEntity<String> implements RelationDefineEntity {
    private static final long serialVersionUID = -8372686525577214172L;
    //关系名称
    @Column
    private String name;
    //关系类型ID
    @Column(name = "type_id", length = 32)
    private String typeId;
    //状态
    @Column
    private Byte status;
}