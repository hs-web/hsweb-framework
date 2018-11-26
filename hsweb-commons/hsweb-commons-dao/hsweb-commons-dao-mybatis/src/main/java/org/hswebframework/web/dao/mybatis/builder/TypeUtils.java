package org.hswebframework.web.dao.mybatis.builder;

import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.JDBCType;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhouhao
 * @since 3.0.3
 */
public class TypeUtils {
    private static final List<Class> numberType = Arrays.asList(
            byte.class, Byte.class
            , short.class, Short.class
            , int.class, Integer.class
            , float.class, Float.class
            , double.class, Double.class
            , long.class, Long.class
            , BigDecimal.class, BigInteger.class
    );

    private static final List<JDBCType> numberJdbcType = Arrays.asList(
            JDBCType.TINYINT, JDBCType.DECIMAL, JDBCType.NUMERIC,
            JDBCType.BIGINT, JDBCType.SMALLINT, JDBCType.INTEGER,
            JDBCType.DECIMAL, JDBCType.BIT
    );

    public static boolean isNumberType(RDBColumnMetaData columnMetaData) {
        return numberType.contains(columnMetaData.getJavaType())
                || Number.class.isAssignableFrom(columnMetaData.getJavaType())
                || numberJdbcType.contains(columnMetaData.getJdbcType());
    }

}
