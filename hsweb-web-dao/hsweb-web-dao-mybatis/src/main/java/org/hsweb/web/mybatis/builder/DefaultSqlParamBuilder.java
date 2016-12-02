package org.hsweb.web.mybatis.builder;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.hsweb.commons.DateTimeUtils;
import org.hsweb.commons.StringUtils;
import org.hsweb.ezorm.core.param.Param;
import org.hsweb.ezorm.core.param.Sort;
import org.hsweb.ezorm.core.param.Term;
import org.hsweb.ezorm.rdb.meta.RDBColumnMetaData;
import org.hsweb.ezorm.rdb.render.dialect.Dialect;
import org.hsweb.web.bean.common.InsertParam;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.common.UpdateParam;
import org.hsweb.web.mybatis.utils.ResultMapsUtils;
import org.hsweb.web.mybatis.utils.SqlAppender;

import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.*;

@Deprecated
public class DefaultSqlParamBuilder {

    public Dialect getDialect() {
        return Dialect.ORACLE;
    }

    public boolean filedToUpperCase() {
        return true;
    }

    protected static final Map<Class, String> simpleName = new HashMap<>();

    private static DefaultSqlParamBuilder instance = new DefaultSqlParamBuilder();

    protected PropertyUtilsBean propertyUtils = BeanUtilsBean.getInstance().getPropertyUtils();

    public DefaultSqlParamBuilder() {
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

    public static DefaultSqlParamBuilder instance() {
        return instance;
    }

    public String encodeFiled(String field) {
        if (field.contains(".")) {
            String[] tmp = field.split("[.]");
            return tmp[0] + "." + getDialect().getQuoteStart() + (filedToUpperCase() ? (tmp[1].toUpperCase()) : tmp[1]) + getDialect().getQuoteEnd();
        } else {
            return getDialect().getQuoteStart() + (filedToUpperCase() ? (field.toUpperCase()) : field) + getDialect().getQuoteEnd();
        }
    }

    public KeyWordMapper getKeyWordMapper(String type) {
        return (paramKey, tableName, term, jdbcType) -> {
            String termField = term.getColumn();
            if (termField.contains(".")) {
                String[] tmp = termField.split("[.]");
                tableName = tmp[0];
                termField = tmp[1];
            }
            RDBColumnMetaData field = new RDBColumnMetaData();
            field.setName(termField);
            field.setJdbcType(jdbcType);
            return getDialect().buildCondition(paramKey, term, field, tableName).toString();
        };
    }

    protected Map<String, Object> createConfig(String resultMapId) {
        ResultMap resultMaps = ResultMapsUtils.getResultMap(resultMapId);
        Map<String, Object> fieldConfig = new HashMap<>();
        List<ResultMapping> resultMappings = new ArrayList<>(resultMaps.getResultMappings());
        resultMappings.addAll(resultMaps.getIdResultMappings());
        resultMappings.forEach(resultMapping -> {
            if (resultMapping.getNestedQueryId() == null) {
                Map<String, Object> config = new HashMap<>();
                config.put("jdbcType", resultMapping.getJdbcType());
                config.put("javaType", getJavaType(resultMapping.getJavaType()));
                config.put("property", resultMapping.getProperty());
                fieldConfig.put(resultMapping.getColumn(), config);
            }
        });
        return fieldConfig;
    }

    public String buildWhere(String resultMapId, String tableName, List<Term> terms) {
        Map<String, Object> fieldConfig = createConfig(resultMapId);
        SqlAppender sqlAppender = new SqlAppender();
        buildWhere(fieldConfig, "", tableName, terms, sqlAppender);
        if (sqlAppender.size() > 0) sqlAppender.removeFirst();
        return sqlAppender.toString();
    }

    public String buildInsertSql(String resultMapId, InsertParam param) {
        ResultMap resultMaps = ResultMapsUtils.getResultMap(resultMapId);
        Map<String, ResultMapping> mappings = new HashMap<>();
        resultMaps.getResultMappings().forEach(resultMapping -> {
            if (resultMapping.getNestedQueryId() == null && !resultMapping.getProperty().contains(".")) {
                mappings.put(resultMapping.getColumn(), resultMapping);
            }
        });
        Object data = param.getData();
        List<Object> listData;
        if (data instanceof Collection) {
            listData = new ArrayList<>(((Collection) data));
        } else {
            listData = Arrays.asList(param.getData());
        }
        param.setData(listData);
        String fields = mappings.keySet().stream()
                .map(str -> new SqlAppender().add(encodeFiled(str), "").toString())
                .reduce((f1, f2) -> new SqlAppender().add(f1, ",", f2)
                        .toString()).get();
        //批量
        int size = listData.size();
        SqlAppender batchSql = new SqlAppender();
        batchSql.add("(", fields, ")values");
        for (int i = 0; i < size; i++) {
            int index = i;
            if (i > 0) {
                batchSql.add(",");
            }
            String values = mappings.keySet().stream().map((f1) -> {
                SqlAppender appender = new SqlAppender();
                ResultMapping mapping = mappings.get(f1);
                appender.add("#{data[" + index + "].",
                        mapping.getProperty(),
                        ",javaType=", getJavaType(mapping.getJavaType()),
                        ",jdbcType=", mapping.getJdbcType(),
                        "}");
                return appender.toString();
            }).reduce((s1, s2) -> s1 + "," + s2).get();
            batchSql.add("(", values, ")");
        }
        return batchSql.toString();
    }

    protected String getJavaType(Class type) {
        String javaType = simpleName.get(type);
        if (javaType == null) javaType = type.getName();
        return javaType;
    }

    public String buildSelectFields(String resultMapId, String tableName, Param param) {
        Map<String, Object> fieldConfig = createConfig(resultMapId);
        if (param == null) return "*";
        Map<String, String> propertyMapper = getPropertyMapper(fieldConfig, param);
        SqlAppender appender = new SqlAppender();
        propertyMapper.forEach((k, v) -> {
            if (!appender.isEmpty())
                appender.add(",");
            if (!k.contains(".") || k.split("[.]")[0].equals(tableName)) {
                appender.add(tableName, ".", encodeFiled(k), " as ");
            } else {
                appender.add(encodeFiled(k), " as ");
            }
            appender.addEdSpc(getDialect().getQuoteStart(), k, getDialect().getQuoteEnd());
        });
        if (appender.isEmpty()) return "*";
        return appender.toString();
    }

    public String buildUpdateFields(String resultMapId, UpdateParam param) throws Exception {
        Map<String, Object> fieldConfig = createConfig(resultMapId);
        param.excludes("id");
        Map<String, String> propertyMapper = getPropertyMapper(fieldConfig, param);
        SqlAppender appender = new SqlAppender();
        propertyMapper.forEach((k, v) -> {
            try {
                if (v.contains(".")) return;
                Object obj = propertyUtils.getProperty(param.getData(), v);
                if (obj != null) {
                    if (!appender.isEmpty())
                        appender.add(",");
                    Map<String, Object> config = ((Map) fieldConfig.get(k));
                    appender.add(encodeFiled(k), "=", "#{data.", v);
                    if (config != null) {
                        Object jdbcType = config.get("jdbcType"),
                                javaType = config.get("javaType");
                        if (jdbcType != null) {
                            appender.add(",jdbcType=", jdbcType);
                        }
                        if (javaType != null) {
                            appender.add(",javaType=", javaType);
                        }
                    }
                    appender.add("}");
                }
            } catch (Exception e) {
            }
        });
        if (appender.isEmpty()) throw new SQLException("未指定列");
        return appender.toString();
    }

    public String buildOrder(String resultMapId, String tableName, QueryParam param) throws Exception {
        Map<String, Object> fieldConfig = createConfig(resultMapId);
        QueryParam tmp = new QueryParam();
        tmp.setSorts(param.getSorts());
        Map<String, String> propertyMapper = getPropertyMapper(fieldConfig, tmp);
        if (tmp.getSorts().isEmpty()) return "";
        Set<Sort> sorts = new LinkedHashSet<>();
        param.getSorts().forEach(sort -> {
            String fieldName = sort.getName();
            if (StringUtils.isNullOrEmpty(fieldName)) return;
            if (fieldName.contains("."))
                fieldName = fieldName.split("[.]")[1];
            if (propertyMapper.containsKey(fieldName) || propertyMapper.containsValue(fieldName)) {
                if (propertyMapper.get(fieldName) == null) {
                    for (Map.Entry<String, String> entry : propertyMapper.entrySet()) {
                        if (entry.getValue().equals(fieldName)) {
                            sort.setName(entry.getKey());
                        }
                    }
                }
                sorts.add(sort);
            }
        });
        if (sorts.isEmpty()) return "";
        String sql = sorts.stream()
                .map(sort -> {
                    String fieldName = sort.getName();
                    if (fieldName.contains("."))
                        fieldName = fieldName.split("[.]")[1];
                    return new SqlAppender()
                            .add(tableName, ".", fieldName, " ", sort.getOrder()).toString();
                })
                .reduce((s, s1) -> new SqlAppender().add(s, ",", s1).toString()).get();
        return " order by ".concat(sql);
    }

    public Map<String, String> getPropertyMapper(Map<String, Object> fieldConfig, Param param) {
        Set<String> includes = param.getIncludes(),
                excludes = param.getExcludes();
        boolean includesIsEmpty = includes.isEmpty(),
                excludesIsEmpty = excludes.isEmpty();
        Map<String, String> propertyMapper = new HashMap<>();
        fieldConfig.forEach((k, v) -> {
            Map<String, Object> config = ((Map) v);
            String fieldName = (String) config.get("property");
            if (fieldName == null) fieldName = k;
            if (includesIsEmpty && excludesIsEmpty) {
                propertyMapper.put(k, fieldName);
                return;
            }
            if (excludes.contains(fieldName) || excludes.contains(k)) {
                return;
            }
            if (includesIsEmpty) {
                propertyMapper.put(k, fieldName);
            } else if (includes.contains(fieldName) || includes.contains(k)) {
                propertyMapper.put(k, fieldName);
            }
        });
        return propertyMapper;
    }

    public JDBCType getFieldJDBCType(String field, Map<String, Object> fieldConfig) {
        if (field == null) return JDBCType.NULL;
        Object conf = fieldConfig.get(field);
        if (conf instanceof Map) {
            try {
                return JDBCType.valueOf(String.valueOf(((Map) conf).get("jdbcType")));
            } catch (Exception e) {
            }
        }
        return JDBCType.VARCHAR;
    }

    public String getColumn(Map<String, Object> fieldConfig, String name) {
        if (name == null) return null;
        Map<String, Object> config = ((Map) fieldConfig.get(name));
        if (config == null) {
            for (Map.Entry<String, Object> entry : fieldConfig.entrySet()) {
                String fieldName = (String) ((Map) entry.getValue()).get("property");
                if (name.equals(fieldName)) {
                    return entry.getKey();
                }
            }
        }
        return name;
    }

    public void buildWhere(Map<String, Object> fieldConfig, String prefix, String tableName, List<Term> terms, SqlAppender appender) {
        if (terms == null || terms.isEmpty()) return;
        int index = 0;
        String prefixTmp = StringUtils.concat(prefix, StringUtils.isNullOrEmpty(prefix) ? "" : ".");
        for (Term term : terms) {
            String column = getColumn(fieldConfig, term.getColumn());
            if (column != null) term.setColumn(column);
            boolean nullTerm = StringUtils.isNullOrEmpty(term.getColumn());
            //不是空条件 也不是可选字段
            if (!nullTerm && !fieldConfig.containsKey(term.getColumn())) continue;
            //不是空条件，值为空
            if (!nullTerm && StringUtils.isNullOrEmpty(term.getValue())) continue;
            //是空条件，但是无嵌套
            if (nullTerm && term.getTerms().isEmpty()) continue;
            //用于sql预编译的参数名
            prefix = StringUtils.concat(prefixTmp, "terms[", index++, "]");
            //JDBC类型
            JDBCType jdbcType = getFieldJDBCType(term.getColumn(), fieldConfig);
            //转换参数的值
            term.setValue(transformationValue(jdbcType, term.getValue()));
            //添加类型，and 或者 or
            appender.add(StringUtils.concat(" ", term.getType().toString(), " "));
            if (!term.getTerms().isEmpty()) {
                //构建嵌套的条件
                SqlAppender nest = new SqlAppender();
                buildWhere(fieldConfig, prefix, tableName, term.getTerms(), nest);
                //如果嵌套结果为空
                if (nest.isEmpty()) {
                    appender.removeLast();//删除最后一个（and 或者 or）
                    continue;
                }
                if (nullTerm) {
                    //删除 第一个（and 或者 or）
                    nest.removeFirst();
                }
                appender.add("(");
                if (!nullTerm)
                    appender.add(getKeyWordMapper(term.getTermType()).fieldMapper(prefix, tableName, term, jdbcType));
                appender.addAll(nest);
                appender.add(")");
            } else {
                if (!nullTerm)
                    appender.add(getKeyWordMapper(term.getTermType()).fieldMapper(prefix, tableName, term, jdbcType));
            }
        }
    }

    protected Object transformationValue(JDBCType type, Object value) {
        switch (type) {
            case INTEGER:
            case NUMERIC:
                if (StringUtils.isInt(type)) return StringUtils.toInt(value);
                if (StringUtils.isDouble(type)) return StringUtils.toDouble(value);
                break;
            case TIMESTAMP:
            case TIME:
            case DATE:
                if (!(value instanceof Date)) {
                    String strValue = String.valueOf(value);
                    Date date = DateTimeUtils.formatUnknownString2Date(strValue);
                    if (date != null) return date;
                }
                break;
        }
        return value;
    }

    public interface KeyWordMapper {
        String fieldMapper(String paramKey, String tableName, Term term, JDBCType jdbcType);
    }
}
