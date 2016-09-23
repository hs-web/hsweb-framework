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


import javax.sql.DataSource;

public class DynamicDataSourceHolder {

    private static DynamicDataSource dynamicDataSource;

    public static DataSource getActiveSource() {
        if (dynamicDataSource != null) {
            return dynamicDataSource.getActiveDataSource();
        }
        return null;
    }

    public static void install(DynamicDataSource dynamicDataSource) {
        if (DynamicDataSourceHolder.dynamicDataSource != null) {
            throw new UnsupportedOperationException();
        }
        DynamicDataSourceHolder.dynamicDataSource = dynamicDataSource;
    }
}
