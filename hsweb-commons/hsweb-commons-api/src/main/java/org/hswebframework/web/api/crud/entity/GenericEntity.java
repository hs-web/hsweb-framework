/*
 *
 *  * Copyright 2019 http://www.hswebframework.org
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.hswebframework.web.api.crud.entity;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.bean.ToString;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


/**
 * @author zhouhao
 * @since 4.0
 */
@Getter
@Setter
public class GenericEntity<PK> implements Entity {

    @Column(length = 64,updatable = false)
    @Id
    @GeneratedValue(generator = "default_id")
    private PK id;

    public String toString(String... ignoreProperty) {
        return ToString.toString(this, ignoreProperty);
    }

    @Override
    public String toString() {
        return ToString.toString(this);
    }
}
