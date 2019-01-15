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

package org.hswebframework.web.service;

import org.hswebframework.ezorm.core.dsl.Update;
import org.hswebframework.web.dao.dynamic.UpdateByEntityDao;
import org.hswebframework.web.commons.entity.param.UpdateParamEntity;

/**
 * 默认的DSL方式更新服务
 *
 * @author zhouhao
 */
public interface DefaultDSLUpdateService<E, PK> extends UpdateService<E, PK> {

    UpdateByEntityDao getDao();

    default Update<E, UpdateParamEntity<E>> createUpdate(E data) {
        return createUpdate(getDao(), data);
    }

    default Update<E, UpdateParamEntity<E>> createUpdate() {
        return createUpdate(getDao());
    }

    static <E> Update<E, UpdateParamEntity<E>> createUpdate(UpdateByEntityDao dao) {
        return Update.build(dao::update, new UpdateParamEntity<>());
    }

    static <E> Update<E, UpdateParamEntity<E>> createUpdate(UpdateByEntityDao dao, E data) {
        return Update.build(dao::update, new UpdateParamEntity<>(data));
    }
}
