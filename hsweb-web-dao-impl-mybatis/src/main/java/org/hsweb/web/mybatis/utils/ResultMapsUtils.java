package org.hsweb.web.mybatis.utils;

import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * Created by zhouhao on 16-6-3.
 */
public class ResultMapsUtils {
    private static SqlSession sqlSession;

    public static ResultMap getResultMap(String id) {
        while (sqlSession == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        return sqlSession.getConfiguration().getResultMap(id);
    }

    public static void setSqlSession(SqlSessionTemplate sqlSession) {
        ResultMapsUtils.sqlSession = sqlSession;
    }
}
