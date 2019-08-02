package org.hswebframework.web.dao.mybatis.handler;

import org.apache.ibatis.type.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Alias("numberBooleanTypeHandler")
@MappedTypes({Boolean.class})
@MappedJdbcTypes({JdbcType.NUMERIC, JdbcType.BOOLEAN})
public class NumberBooleanTypeHandler implements TypeHandler<Object> {
    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, jdbcType.TYPE_CODE);
            return;
        }
        if(parameter instanceof Number){
            if (jdbcType == JdbcType.BOOLEAN) {
                ps.setBoolean(i, ((Number) parameter).intValue()==1);
            }else{
                ps.setInt(i,((Number) parameter).intValue());
            }
        }else{
            if (jdbcType == JdbcType.BOOLEAN) {
                ps.setBoolean(i, Boolean.TRUE.equals(parameter));
            } else {
                ps.setInt(i, Boolean.TRUE.equals(parameter) ? 1 : 0);
            }
        }
    }

    @Override
    public Object getResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getBoolean(columnName);
    }

    @Override
    public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getBoolean(columnIndex);
    }

    @Override
    public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getBoolean(columnIndex);
    }
}
