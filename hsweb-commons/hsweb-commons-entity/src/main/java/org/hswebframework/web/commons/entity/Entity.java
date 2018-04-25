/*
 *
 *  * Copyright 2016 http://www.hswebframework.org
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

package org.hswebframework.web.commons.entity;

import org.hswebframework.web.commons.bean.Bean;
import org.hswebframework.web.commons.bean.ValidateBean;

import java.io.Serializable;

/**
 * 实体总接口,所有实体需实现此接口
 *
 * @author zhouhao
 * @see org.hswebframework.web.commons.entity.factory.EntityFactory
 * @see GenericEntity
 * @see TreeSupportEntity
 * @see TreeSortSupportEntity
 * @see Bean
 * @since 3.0
 */
public interface Entity extends ValidateBean {


}
