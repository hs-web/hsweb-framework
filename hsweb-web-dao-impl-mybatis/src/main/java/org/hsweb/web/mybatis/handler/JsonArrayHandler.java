package org.hsweb.web.mybatis.handler;

import com.alibaba.fastjson.JSON;
import org.apache.ibatis.type.Alias;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouhao on 16-5-14.
 */
@Alias("jsonArrayHandler")
@MappedTypes({List.class})
public class JsonArrayHandler extends BaseTypeHandler<List<Object>> {

    @Override
    public List<Object> getResult(ResultSet rs, int columnIndex) throws SQLException {
        String s = rs.getString(columnIndex);
        return JSON.parseArray(s);
    }

    @Override
    public List<Object> getResult(ResultSet rs, String columnName) throws SQLException {
        String s = rs.getString(columnName);
        return JSON.parseArray(s);
    }

    @Override
    public List<Object> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String s = cs.getString(columnIndex);
        return JSON.parseArray(s);
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, List<Object> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JSON.toJSONString(parameter));
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Object> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, "[]");
    }

    @Override
    public List<Object> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return new ArrayList<>();
    }

    @Override
    public List<Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return new ArrayList<>();
    }

    @Override
    public List<Object> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return new ArrayList<>();
    }
}
