package org.hswebframework.web.entity.organizational;

import lombok.*;
import org.hswebframework.web.commons.entity.SimpleTreeSortSupportEntity;

import java.util.List;

/**
 * 表单发布日志
 *
 * @author hsweb-generator-online
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleDistrictEntity extends SimpleTreeSortSupportEntity<String> implements DistrictEntity {
    //区域名称,如重庆市
    private String name;
    //区域全程,如重庆市江津区
    private String fullName;
    //区域级别名称,如:省
    private String levelName;
    //区域级别编码,如:province
    private String levelCode;
    //行政区域代码,如:500000
    private String code;
    //说明
    private String describe;
    //状态
    private Byte status;
    private List<DistrictEntity> children;

}