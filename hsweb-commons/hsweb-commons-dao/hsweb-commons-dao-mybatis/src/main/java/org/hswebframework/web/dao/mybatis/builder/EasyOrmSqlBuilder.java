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

package org.hswebframework.web.dao.mybatis.builder;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.hswebframework.ezorm.core.param.InsertParam;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.core.param.UpdateParam;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.meta.RDBDatabaseMetaData;
import org.hswebframework.ezorm.rdb.meta.RDBTableMetaData;
import org.hswebframework.ezorm.rdb.meta.converter.DateTimeConverter;
import org.hswebframework.ezorm.rdb.meta.converter.NumberValueConverter;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.SqlRender;
import org.hswebframework.ezorm.rdb.render.dialect.Dialect;
import org.hswebframework.ezorm.rdb.render.dialect.H2RDBDatabaseMetaData;
import org.hswebframework.ezorm.rdb.render.dialect.MysqlRDBDatabaseMetaData;
import org.hswebframework.ezorm.rdb.render.dialect.OracleRDBDatabaseMetaData;
import org.hswebframework.ezorm.rdb.render.support.simple.CommonSqlRender;
import org.hswebframework.ezorm.rdb.render.support.simple.SimpleWhereSqlBuilder;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.dao.mybatis.plgins.pager.Pager;
import org.hswebframework.web.dao.mybatis.utils.ResultMapsUtils;
import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.datasource.DatabaseType;

import java.sql.JDBCType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 使用easyorm 动态构建 sql
 *
 * @author zhouhao
 * @since 2.0
 */
public class EasyOrmSqlBuilder {

    private static final   EasyOrmSqlBuilder  instance   = new EasyOrmSqlBuilder();
    protected static final Map<Class, String> simpleName = new HashMap<>();

    protected PropertyUtilsBean propertyUtils = BeanUtilsBean.getInstance().getPropertyUtils();

    public static EasyOrmSqlBuilder getInstance() {
        return instance;
    }

    private EasyOrmSqlBuilder() {
    }

    static {
        simpleName.put(Integer.class, "int");
        simpleName.put(Byte.class, "byte");
        simpleName.put(Double.class, "double");
        simpleName.put(Float.class, "float");
        simpleName.put(Boolean.class, "boolean");
        simpleName.put(Long.class, "long");
        simpleName.put(Short.class, "short");
        simpleName.put(Character.class, "char");
        simpleName.put(String.class, "string");
        simpleName.put(int.class, "int");
        simpleName.put(double.class, "double");
        simpleName.put(float.class, "float");
        simpleName.put(boolean.class, "boolean");
        simpleName.put(long.class, "long");
        simpleName.put(short.class, "short");
        simpleName.put(char.class, "char");
        simpleName.put(byte.class, "byte");
    }

    public static String getJavaType(Class type) {
        String javaType = simpleName.get(type);
        if (javaType == null) javaType = type.getName();
        return javaType;
    }

    private final RDBDatabaseMetaData mysql  = new MysqlMeta();
    private final RDBDatabaseMetaData oracle = new OracleMeta();
    private final RDBDatabaseMetaData h2     = new H2Meta();

    private final ConcurrentMap<RDBDatabaseMetaData, Map<String, RDBTableMetaData>> metaCache = new ConcurrentHashMap<RDBDatabaseMetaData, Map<String, RDBTableMetaData>>() {
        @Override
        public Map<String, RDBTableMetaData> get(Object key) {
            Map<String, RDBTableMetaData> map = super.get(key);
            if (map == null) {
                map = new HashMap<>();
                put((RDBDatabaseMetaData) key, map);
            }
            return map;
        }
    };

    public RDBDatabaseMetaData getActiveDatabase() {
        DatabaseType type = DataSourceHolder.currentDatabaseType();
        switch (type) {
            case h2:
                return h2;
            case mysql:
                return mysql;
            case oracle:
                return oracle;
            default:
                return h2;
        }
    }

    protected RDBTableMetaData createMeta(String tableName, String resultMapId) {
        RDBDatabaseMetaData active = getActiveDatabase();
        String cacheKey = tableName.concat("-").concat(resultMapId);
        Map<String, RDBTableMetaData> cache = metaCache.get(active);
        RDBTableMetaData cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        RDBTableMetaData rdbTableMetaData = new RDBTableMetaData();
        rdbTableMetaData.setName(tableName);
        rdbTableMetaData.setDatabaseMetaData(active);
        ResultMap resultMaps = ResultMapsUtils.getResultMap(resultMapId);
        List<ResultMapping> resultMappings = new ArrayList<>(resultMaps.getResultMappings());
        resultMappings.addAll(resultMaps.getIdResultMappings());
        resultMappings.forEach(resultMapping -> {
            if (resultMapping.getNestedQueryId() == null) {
                RDBColumnMetaData column = new RDBColumnMetaData();
                column.setJdbcType(JDBCType.valueOf(resultMapping.getJdbcType().name()));
                column.setName(resultMapping.getColumn());
                if (!StringUtils.isNullOrEmpty(resultMapping.getProperty()))
                    column.setAlias(resultMapping.getProperty());
                column.setJavaType(resultMapping.getJavaType());
                column.setProperty("resultMapping", resultMapping);
                if (column.getJdbcType() == JDBCType.DATE) {
                    column.setValueConverter(new DateTimeConverter("yyyy-MM-dd", column.getJavaType()));
                } else if (column.getJdbcType() == JDBCType.TIMESTAMP) {
                    column.setValueConverter(new DateTimeConverter("yyyy-MM-dd HH:mm:ss", column.getJavaType()));
                } else if (column.getJdbcType() == JDBCType.NUMERIC) {
                    column.setValueConverter(new NumberValueConverter(column.getJavaType()));
                }
                rdbTableMetaData.addColumn(column);
            }
        });
        cache.put(cacheKey, rdbTableMetaData);
        return rdbTableMetaData;
    }

    public String buildUpdateFields(String resultMapId, String tableName, UpdateParam param) {
        Pager.reset();
        param.excludes("id");
        RDBTableMetaData tableMetaData = createMeta(tableName, resultMapId);
        RDBDatabaseMetaData databaseMetaDate = getActiveDatabase();
        Dialect dialect = databaseMetaDate.getDialect();
        CommonSqlRender render = (CommonSqlRender) databaseMetaDate.getRenderer(SqlRender.TYPE.SELECT);
        List<CommonSqlRender.OperationColumn> columns = render.parseOperationField(tableMetaData, param);
        SqlAppender appender = new SqlAppender();
        columns.forEach(column -> {
            RDBColumnMetaData columnMetaData = column.getRDBColumnMetaData();
            if (columnMetaData == null) return;
            if (columnMetaData.getName().contains(".")) return;
            try {
                Object tmp = propertyUtils.getProperty(param.getData(), columnMetaData.getAlias());
                if (tmp == null) return;
            } catch (Exception e) {
                return;
            }
            appender.add(",", encodeColumn(dialect, columnMetaData.getName())
                    , "=", "#{data.", columnMetaData.getAlias(),
                    ",javaType=", EasyOrmSqlBuilder.getJavaType(columnMetaData.getJavaType()),
                    ",jdbcType=", columnMetaData.getJdbcType(),
                    "}");
        });
        if (!appender.isEmpty()) appender.removeFirst();
        else throw new UnsupportedOperationException("{no_columns_will_be_update}");
        return appender.toString();
    }

    public String encodeColumn(Dialect dialect, String field) {
        if (field.contains(".")) {
            String[] tmp = field.split("[.]");
            return tmp[0] + "." + dialect.getQuoteStart() + (dialect.columnToUpperCase() ? (tmp[1].toUpperCase()) : tmp[1]) + dialect.getQuoteEnd();
        } else {
            return dialect.getQuoteStart() + (dialect.columnToUpperCase() ? (field.toUpperCase()) : field) + dialect.getQuoteEnd();
        }
    }

    public String buildInsertSql(String resultMapId, String tableName, Object param) {
        Pager.reset();
        InsertParam insertParam;
        if (param instanceof InsertParam)
            insertParam = ((InsertParam) param);
        else
            insertParam = new InsertParam<>(param);
        RDBTableMetaData tableMetaData = createMeta(tableName, resultMapId);
        SqlRender<InsertParam> render = tableMetaData.getDatabaseMetaData().getRenderer(SqlRender.TYPE.INSERT);
        String sql = render.render(tableMetaData, insertParam).getSql();
        return sql;
    }

    public String buildUpdateSql(String resultMapId, String tableName, UpdateParam param) {
        Pager.reset();
        RDBTableMetaData tableMetaData = createMeta(tableName, resultMapId);
        SqlRender<UpdateParam> render = tableMetaData.getDatabaseMetaData().getRenderer(SqlRender.TYPE.UPDATE);
        return render.render(tableMetaData, param).getSql();
    }

    public String buildSelectFields(String resultMapId, String tableName, QueryParam param) {
        if (param == null) {
            return "*";
        }
        if (param.isPaging() && Pager.get() == null) {
            Pager.doPaging(param.getPageIndex(), param.getPageSize());
        } else {
            Pager.reset();
        }
        RDBTableMetaData tableMetaData = createMeta(tableName, resultMapId);
        RDBDatabaseMetaData databaseMetaDate = getActiveDatabase();
        Dialect dialect = databaseMetaDate.getDialect();
        CommonSqlRender render = (CommonSqlRender) databaseMetaDate.getRenderer(SqlRender.TYPE.SELECT);
        List<CommonSqlRender.OperationColumn> columns = render.parseOperationField(tableMetaData, param);
        SqlAppender appender = new SqlAppender();
        columns.forEach(column -> {
            RDBColumnMetaData columnMetaData = column.getRDBColumnMetaData();
            if (columnMetaData == null) return;
            String cname = columnMetaData.getName();
            if (!cname.contains(".")) cname = tableName.concat(".").concat(cname);
            appender.add(",", encodeColumn(dialect, cname)
                    , " AS "
                    , dialect.getQuoteStart()
                    , columnMetaData.getName()
                    , dialect.getQuoteEnd());
        });
        param.getIncludes().remove("*");
        if (appender.isEmpty()) return "*";
        appender.removeFirst();
        return appender.toString();
    }

    public String buildOrder(String resultMapId, String tableName, QueryParam param) {
        if (param == null) {
            return "";
        }
        RDBTableMetaData tableMetaData = createMeta(tableName, resultMapId);
        SqlAppender appender = new SqlAppender(" order by ");
        param.getSorts()
                .forEach(sort -> {
                    RDBColumnMetaData column = tableMetaData.getColumn(sort.getName());
                    if (column == null)
                        column = tableMetaData.findColumn(sort.getName());
                    if (column == null) return;
                    String cname = column.getName();
                    if (!cname.contains(".")) cname = tableName.concat(".").concat(cname);
                    appender.add(encodeColumn(tableMetaData.getDatabaseMetaData().getDialect(), cname), " ", sort.getOrder(), ",");
                });
        if (appender.isEmpty()) return "";
        appender.removeLast();
        return appender.toString();
    }

    public String buildWhereForUpdate(String resultMapId, String tableName, List<Term> terms) {
        String where = buildWhere(resultMapId, tableName, terms);
        if (where.trim().isEmpty()) {
            throw new BusinessException("禁止执行无条件的更新操作");
        }
        return where;
    }

    public String buildWhere(String resultMapId, String tableName, List<Term> terms) {
        RDBTableMetaData tableMetaData = createMeta(tableName, resultMapId);
        RDBDatabaseMetaData databaseMetaDate = getActiveDatabase();
        SimpleWhereSqlBuilder builder = new SimpleWhereSqlBuilder() {
            @Override
            public Dialect getDialect() {
                return databaseMetaDate.getDialect();
            }
        };
        SqlAppender appender = new SqlAppender();
        builder.buildWhere(tableMetaData, "", terms, appender, new HashSet<>());
        return appender.toString();
    }

    class MysqlMeta extends MysqlRDBDatabaseMetaData {
        public MysqlMeta() {
            super();
            renderMap.put(SqlRender.TYPE.INSERT, new InsertSqlBuilder());
            renderMap.put(SqlRender.TYPE.UPDATE, new UpdateSqlBuilder(Dialect.MYSQL));
        }
    }

    class OracleMeta extends OracleRDBDatabaseMetaData {
        public OracleMeta() {
            super();
            renderMap.put(SqlRender.TYPE.INSERT, new InsertSqlBuilder());
            renderMap.put(SqlRender.TYPE.UPDATE, new UpdateSqlBuilder(Dialect.ORACLE));
        }
    }

    class H2Meta extends H2RDBDatabaseMetaData {
        public H2Meta() {
            super();
            renderMap.put(SqlRender.TYPE.INSERT, new InsertSqlBuilder());
            renderMap.put(SqlRender.TYPE.UPDATE, new UpdateSqlBuilder(Dialect.H2));
        }
    }
}
