/*
 *  Copyright 2019 http://www.hswebframework.org
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package org.hswebframework.web.entity.organizational;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleTreeSortSupportEntity;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.List;

/**
 * 组织
 *
 * @author hsweb-generator-online
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "s_organization",indexes = {
        @Index(name = "idx_org_name",columnList = "name"),
        @Index(name = "idx_org_path",columnList = "path"),
        @Index(name = "idx_org_parent_id",columnList = "parent_id"),
        @Index(name = "idx_org_area_id",columnList = "area_id"),

})
public class SimpleOrganizationalEntity extends SimpleTreeSortSupportEntity<String> implements OrganizationalEntity {
    private static final long serialVersionUID = -1610547249282278768L;
    //名称
    @Column
    private String name;
    //全称
    @Column(name = "full_name")
    private String fullName;
    //机构编码
    @Column
    private String code;
    //是否启用
    @Column
    private Byte status;

    @Column(name = "district_id",length = 32)
    private String districtId;

    //子级组织
    private List<OrganizationalEntity> children;

}