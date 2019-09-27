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

import org.hswebframework.ezorm.rdb.mapping.SyncDelete;
import org.hswebframework.ezorm.rdb.mapping.SyncRepository;

/**
 * @author zhouhao
 */
public interface DefaultDSLDeleteService<E, PK> extends DefaultDeleteService<E, PK> {
    SyncRepository<E,PK> getDao();

    default SyncDelete createDelete() {
        return getDao().createDelete();
    }

    static SyncDelete createDelete(SyncRepository<?,?> deleteDao) {
        return deleteDao.createDelete();
    }


}
