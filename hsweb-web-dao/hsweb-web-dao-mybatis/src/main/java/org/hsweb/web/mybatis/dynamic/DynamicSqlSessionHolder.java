/**
 * Copyright 2010-2015 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hsweb.web.mybatis.dynamic;

import org.apache.ibatis.session.SqlSession;
import org.hsweb.web.core.datasource.DynamicDataSource;
import org.springframework.transaction.support.ResourceHolderSupport;

import java.util.*;

public final class DynamicSqlSessionHolder extends ResourceHolderSupport {

    private Map<String, SqlSession> sqlSessionMap;

    public DynamicSqlSessionHolder() {
        sqlSessionMap = new HashMap<>();
    }

    public SqlSession getSqlSession() {
        return sqlSessionMap.get(getDataSourceId());
    }

    public void remove() {
        sqlSessionMap.remove(getDataSourceId());
    }

    public void remove(String dataSourceId) {
        sqlSessionMap.remove(dataSourceId);
    }

    public Collection<SqlSession> getAllSqlSession() {
        return sqlSessionMap.values();
    }

    public void close() {
        List<Exception> exceptions = new ArrayList<>();
        sqlSessionMap.forEach((id, sqlSession) -> {
            try {
                sqlSession.close();
            } catch (Exception e) {
                exceptions.add(e);
            }
        });
        sqlSessionMap.clear();
        //todo 异常未处理
        //   if (exceptions.size() > 0) throw new RuntimeException(exceptions.get(0));
    }

    public void commit() {
        sqlSessionMap.values().forEach(SqlSession::commit);
    }

    public void setSqlSession(SqlSession sqlSession) {
        sqlSessionMap.put(getDataSourceId(), sqlSession);
    }


    public void setSqlSession(String dataSourceId, SqlSession sqlSession) {
        sqlSessionMap.put(dataSourceId, sqlSession);
    }

    public String getDataSourceId() {
        String id = DynamicDataSource.getActiveDataSourceId();
        if (null == id) return "default";
        return id;
    }
}
