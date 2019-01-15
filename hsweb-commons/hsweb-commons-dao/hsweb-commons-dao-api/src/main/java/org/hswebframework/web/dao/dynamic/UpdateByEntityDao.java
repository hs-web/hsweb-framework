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

package org.hswebframework.web.dao.dynamic;

import org.hswebframework.web.commons.entity.Entity;

/**
 * 根据实体类进行更新,实体类支持动态条件或者普通实体类。
 * 动态条件和{@link QueryByEntityDao#query(Entity)} 一致。
 *
 * @author zhouhao
 * @since 3.0
 */
public interface UpdateByEntityDao {
    int update(Entity entity);
}
