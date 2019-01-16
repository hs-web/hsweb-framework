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

package org.hswebframework.web.dao;

/**
 * 通用删除dao
 *
 * @param <PK> 主键类型
 * @author zhouhao
 * @since 3.0
 */
public interface DeleteDao<PK> extends Dao {
    /**
     * 根据主键删除数据,并返回被删除数据的数量
     *
     * @param pk 主键
     * @return 删除的数据数量, 理论上此返回值应该为0或者1.
     */
    int deleteByPk(PK pk);
}
