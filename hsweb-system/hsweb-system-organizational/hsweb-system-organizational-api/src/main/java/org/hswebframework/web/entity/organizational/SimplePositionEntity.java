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
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.List;

/**
 * 职位
 *
 * @author hsweb-generator-online
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "s_position",indexes = {
        @Index(name = "idx_position_dept_id",columnList = "department_id")

})
public class SimplePositionEntity extends SimpleTreeSortSupportEntity<String> implements PositionEntity {
    private static final long serialVersionUID = -8912215943657734192L;
    //职位名称
    @Column
    private String name;
    //部门id
    @Column(name = "department_id" ,length = 32)
    private String departmentId;

    //备注
    @Column
    private String remark;

    private List<PositionEntity> children;

    @Override
    @Id
    @Column(name = "u_id")
    public String getId() {
        return super.getId();
    }
}