/*
 *  Copyright 2019 http://www.hswebframework.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.hswebframework.web.dao.mybatis.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.*;
import org.springframework.util.StringUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Alias("jsonSetHandler")
@MappedTypes({Set.class})
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.CLOB})
@Slf4j
public class JsonSetHandler extends BaseTypeHandler<Set> {

    @SuppressWarnings("unchecked")
    private Set<Object> parseSet(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        return (Set) JSON.parseObject(json, Set.class);
    }

    @Override
    public Set getResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseSet(rs.getString(columnIndex));
    }

    @Override
    public Set getResult(ResultSet rs, String columnName) throws SQLException {
        return parseSet(rs.getString(columnName));
    }

    @Override
    public Set getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseSet(cs.getString(columnIndex));
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, Set parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JSON.toJSONString(parameter, SerializerFeature.WriteClassName));
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Set parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, "[]");
    }

    @Override
    public Set getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return new HashSet<>();
    }

    @Override
    public Set getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return new HashSet<>();
    }

    @Override
    public Set getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return new HashSet<>();
    }
}
