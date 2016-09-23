/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.core.datasource;

import org.hsweb.web.core.utils.ThreadLocalUtils;
import org.springframework.jca.cci.connection.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;

/**
 * @author zhouhao
 */
public interface DynamicDataSource extends DataSource {
    String DATA_SOURCE_FLAG = "data-source-id";

    String DATA_SOURCE_FLAG_LAST = "data-source-id-last";


    static void useLast() {
        use(ThreadLocalUtils.get(DATA_SOURCE_FLAG_LAST));
    }

    static void use(String dataSourceId) {
        ThreadLocalUtils.put(DATA_SOURCE_FLAG, dataSourceId);
    }

    static String getActiveDataSourceId() {
        return ThreadLocalUtils.get(DATA_SOURCE_FLAG);
    }

    static void useDefault(boolean rememberLast) {
        if (getActiveDataSourceId() != null && rememberLast)
            ThreadLocalUtils.put(DATA_SOURCE_FLAG_LAST, getActiveDataSourceId());
        ThreadLocalUtils.remove(DATA_SOURCE_FLAG);
    }

    static void useDefault() {
        useDefault(true);
    }

    DataSource getActiveDataSource();
}
