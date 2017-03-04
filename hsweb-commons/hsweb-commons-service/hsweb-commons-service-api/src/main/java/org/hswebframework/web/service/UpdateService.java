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

package org.hswebframework.web.service;

import java.util.List;

public interface UpdateService<E, PK> extends Service {
    /**
     * 修改记录信息
     *
     * @param data 要修改的对象
     * @return 影响记录数
     */
    int updateByPk(PK id, E data);

    /**
     * 批量修改记录
     *
     * @param data 要修改的记录集合
     * @return 影响记录数
     */
    int updateByPk(List<E> data);

    /**
     * 保存或修改
     *
     * @param e 要修改的数据
     * @return 影响记录数
     */
    PK saveOrUpdate(E e);


}
