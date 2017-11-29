package org.hswebframework.web.entity.organizational;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * 关系信息
 *
 * @author hsweb-generator-online
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleRelationInfoEntity extends SimpleGenericEntity<String> implements RelationInfoEntity {
    private static final long serialVersionUID = -7285786918328019221L;
    //关系从
    private String relationFrom;
    //关系定义id
    private String relationId;
    //关系至
    private String relationTo;
    //关系类型从,如:人员
    private String relationTypeFrom;
    //关系类型至,如:部门
    private String relationTypeTo;
    //状态
    private Byte status;
}