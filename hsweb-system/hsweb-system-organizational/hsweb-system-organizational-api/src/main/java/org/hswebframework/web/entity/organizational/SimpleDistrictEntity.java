package org.hswebframework.web.entity.organizational;

import lombok.*;
import org.hswebframework.web.commons.entity.SimpleTreeSortSupportEntity;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;
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
@Table(name = "s_district",indexes = {
        @Index(name = "idx_district_name",columnList = "name"),
        @Index(name = "idx_district_path",columnList = "path"),
        @Index(name = "idx_district_parent_id",columnList = "parent_id"),

})
public class SimpleDistrictEntity extends SimpleTreeSortSupportEntity<String> implements DistrictEntity {
    //区域名称,如重庆市
    @Column
    private String name;
    @Column(name = "full_name")
    //区域全程,如重庆市江津区
    private String fullName;
    //区域级别名称,如:省
    @Column(name = "level_name")
    private String levelName;
    //区域级别编码,如:province
    @Column(name = "level_code")
    private String levelCode;
    //行政区域代码,如:500000
    @Column
    private String code;
    //说明
    @Column
    private String describe;
    //状态
    @Column
    private Byte status;

    private List<DistrictEntity> children;

}