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
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import org.hswebframework.web.commons.entity.annotation.ImplementFor;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * 人员职位关联
 *
 * @author hsweb-generator-online
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "s_person_position",indexes = {
        @Index(name = "idx_person_per_pid",columnList = "person_id"),
        @Index(name = "idx_person_pos_pid",columnList = "position_id")

})
@ImplementFor(PersonPositionEntity.class)
public class SimplePersonPositionEntity  implements PersonPositionEntity {
    private static final long serialVersionUID = -7102840729564722732L;
    //人员id
    @Column(name = "person_id")
    private String personId;
    //职位id
    @Column(name = "position_id")
    private String positionId;

}