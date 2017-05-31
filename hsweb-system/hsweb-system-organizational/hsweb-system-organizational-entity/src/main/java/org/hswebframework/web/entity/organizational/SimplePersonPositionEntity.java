/*
 *  Copyright 2016 http://www.hswebframework.org
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

import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * 人员职位关联
 *
 * @author hsweb-generator-online
 */
public class SimplePersonPositionEntity extends SimpleGenericEntity<String> implements PersonPositionEntity {
    //人员id
    private String personId;
    //职位id
    private String positionId;

    /**
     * @return 人员id
     */
    public String getPersonId() {
        return this.personId;
    }

    /**
     * 设置 人员id
     */
    public void setPersonId(String personId) {
        this.personId = personId;
    }

    /**
     * @return 职位id
     */
    public String getPositionId() {
        return this.positionId;
    }

    /**
     * 设置 职位id
     */
    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

}