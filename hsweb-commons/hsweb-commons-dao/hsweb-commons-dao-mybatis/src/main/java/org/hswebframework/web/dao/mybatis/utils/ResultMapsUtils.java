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

package org.hswebframework.web.dao.mybatis.utils;

import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.concurrent.CountDownLatch;

/**
 * @since 2.0
 */
public class ResultMapsUtils {
    private volatile static SqlSession sqlSession;

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static ResultMap getResultMap(String id) {
        if (sqlSession == null) {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new UnsupportedOperationException(e);
            }
            if (sqlSession == null) {
                throw new UnsupportedOperationException("sqlSession is null");
            }
        }

        return sqlSession.getConfiguration().getResultMap(id);
    }

    public static void setSqlSession(SqlSessionTemplate sqlSession) {
        ResultMapsUtils.sqlSession = sqlSession;
        if (countDownLatch.getCount() != 0) {
            countDownLatch.countDown();
        }
    }
}
