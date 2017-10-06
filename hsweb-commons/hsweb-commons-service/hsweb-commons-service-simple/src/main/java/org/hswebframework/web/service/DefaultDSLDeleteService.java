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

import org.hswebframework.ezorm.core.dsl.Delete;
import org.hswebframework.web.commons.entity.param.DeleteParamEntity;
import org.hswebframework.web.dao.dynamic.DeleteByEntityDao;

/**
 * @author zhouhao
 */
public interface DefaultDSLDeleteService<PK> extends DefaultDeleteService<PK> {
    DeleteByEntityDao getDao();

    default Delete<DeleteParamEntity> createDelete() {
        Delete<DeleteParamEntity> delete = new Delete<>(new DeleteParamEntity());
        delete.setExecutor(getDao()::delete);
        return delete;
    }

    static Delete<DeleteParamEntity> createDelete(DeleteByEntityDao deleteDao) {
        Delete<DeleteParamEntity> update = new Delete<>(new DeleteParamEntity());
        update.setExecutor(deleteDao::delete);
        return update;
    }

    /**
     * 自定义一个删除执行器。创建dsl数据删除操作对象
     *
     * @param executor 执行器
     * @return {@link Delete}
     * @since 3.0
     */
    static Delete<DeleteParamEntity> createDelete(Delete.Executor<DeleteParamEntity> executor) {
        Delete<DeleteParamEntity> update = new Delete<>(new DeleteParamEntity());
        update.setExecutor(executor);
        return update;
    }

}
