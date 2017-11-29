package org.hswebframework.web.entity.organizational;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * 关系定义
 *
 * @author hsweb-generator-online
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleRelationDefineEntity extends SimpleGenericEntity<String> implements RelationDefineEntity {
    private static final long serialVersionUID = -8372686525577214172L;
    //关系名称
    private String name;
    //关系类型ID
    private String typeId;
    //状态
    private Byte status;
}