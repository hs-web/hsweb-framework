package org.hsweb.web.mybatis.builder;

import org.apache.commons.beanutils.BeanUtils;
import org.hsweb.web.bean.common.*;
import org.hsweb.web.mybatis.utils.SqlAppender;
import org.webbuilder.utils.common.DateTimeUtils;
import org.webbuilder.utils.common.StringUtils;

import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by zhouhao on 16-5-9.
 */
public class DefaultSqlParamBuilder {

    public String getQuoteStart() {
        return "\"";
    }

    public String getQuoteEnd() {
        return "\"";
    }

    protected Map<TermType, KeyWordMapper> mapperMap = new HashMap<>();

    private static DefaultSqlParamBuilder instance = new DefaultSqlParamBuilder();

    public DefaultSqlParamBuilder() {
        mapperMap.put(TermType.eq, (paramKey, tableName, field, jdbcType) ->
                        new SqlAppender().add(tableName, ".", field.getField(), " = ", "#{", paramKey, "}").toString()
        );
        mapperMap.put(TermType.not, (paramKey, tableName, field, jdbcType) ->
                        new SqlAppender().add(tableName, ".", field.getField(), " != ", "#{", paramKey, "}").toString()
        );
        mapperMap.put(TermType.like, (paramKey, tableName, field, jdbcType) ->
                        new SqlAppender().add(tableName, ".", field.getField(), " like ", "#{", paramKey, "}").toString()
        );
        mapperMap.put(TermType.notlike, (paramKey, tableName, field, jdbcType) ->
                        new SqlAppender().add(tableName, ".", field.getField(), " not like ", "#{", paramKey, "}").toString()
        );
        mapperMap.put(TermType.notnull, (paramKey, tableName, field, jdbcType) ->
                        new SqlAppender().add(tableName, ".", field.getField(), " is not null").toString()
        );
        mapperMap.put(TermType.isnull, (paramKey, tableName, field, jdbcType) ->
                        new SqlAppender().add(tableName, ".", field.getField(), " is null").toString()
        );
        mapperMap.put(TermType.empty, (paramKey, tableName, field, jdbcType) ->
                        new SqlAppender().add(tableName, ".", field.getField(), " =''").toString()
        );
        mapperMap.put(TermType.notempty, (paramKey, tableName, field, jdbcType) ->
                        new SqlAppender().add(tableName, ".", field.getField(), " !=''").toString()
        );
        mapperMap.put(TermType.btw, (paramKey, tableName, field, jdbcType) ->
        {
            SqlAppender sqlAppender = new SqlAppender();
            List<Object> objects = param2list(field.getValue());
            if (objects.size() == 1)
                objects.add(objects.get(0));
            field.setValue(objects);
            sqlAppender.add(tableName, ".", field.getField()," ").addSpc("between")
                    .add(" #{", paramKey, "[0]}")
                    .add(" and ", "#{", paramKey, "[1]}");
            return sqlAppender.toString();
        });

        mapperMap.put(TermType.notbtw, (paramKey, tableName, field, jdbcType) ->
        {
            SqlAppender sqlAppender = new SqlAppender();
            List<Object> objects = param2list(field.getValue());
            if (objects.size() == 1)
                objects.add(objects.get(0));
            field.setValue(objects);
            sqlAppender.add(tableName, ".", field.getField()," ").addSpc("not between")
                    .add(" #{", paramKey, "[0]}")
                    .add(" and ", "#{", paramKey, "[1]}");
            return sqlAppender.toString();
        });

        mapperMap.put(TermType.gt, (paramKey, tableName, field, jdbcType) -> {
            SqlAppender sqlAppender = new SqlAppender();
            if (Arrays.<JDBCType>asList(JDBCType.DATE, JDBCType.TIME, JDBCType.TIMESTAMP).contains(jdbcType)) {
                if (!(field.getValue() instanceof Date)) {
                    String strValue = String.valueOf(field.getValue());
                    Date date = DateTimeUtils.formatUnknownString2Date(strValue);
                    if (date != null) field.setValue(date);
                }
            }
            sqlAppender.add(tableName, ".", field.getField(), " >= #{", paramKey, "}");
            return sqlAppender.toString();
        });

        mapperMap.put(TermType.lt, (paramKey, tableName, field, jdbcType) -> {
            SqlAppender sqlAppender = new SqlAppender();
            if (Arrays.<JDBCType>asList(JDBCType.DATE, JDBCType.TIME, JDBCType.TIMESTAMP).contains(jdbcType)) {
                if (!(field.getValue() instanceof Date)) {
                    String strValue = String.valueOf(field.getValue());
                    Date date = DateTimeUtils.formatUnknownString2Date(strValue);
                    if (date != null) field.setValue(date);
                }
            }
            sqlAppender.add(tableName, ".",field.getField(), " <= #{", paramKey, "}");
            return sqlAppender.toString();
        });

        mapperMap.put(TermType.in, (paramKey, tableName, field, jdbcType) -> {
            List<Object> values = param2list(field.getValue());
            field.setValue(values);
            SqlAppender appender = new SqlAppender();
            appender.add(tableName, ".").addSpc(field.getField(), "in(");
            for (int i = 0; i < values.size(); i++) {
                appender.add("#{", paramKey, "[", i, "]}", ",");
            }
            appender.removeLast();
            appender.add(")");
            return appender.toString();
        });

        mapperMap.put(TermType.notin, (paramKey, tableName, field, jdbcType) -> {
            List<Object> values = param2list(field.getValue());
            field.setValue(values);
            SqlAppender appender = new SqlAppender();
            appender.add(tableName, ".").addSpc(field.getField(), "not in(");
            for (int i = 0; i < values.size(); i++) {
                appender.add("#{", paramKey, "[", i, "]}", ",");
            }
            appender.removeLast();
            appender.add(")");
            return appender.toString();
        });

    }

    public static DefaultSqlParamBuilder instance() {
        return instance;
    }

    public KeyWordMapper getKeyWordMapper(TermType type) {
        return mapperMap.get(type);
    }

    public String buildWhere(Map<String, Object> fieldConfig, String tableName, List<Term> terms) {
        SqlAppender sqlAppender = new SqlAppender();
        buildWhere(fieldConfig, "", tableName, terms, sqlAppender);
        if (sqlAppender.size() > 0) sqlAppender.removeFirst();
        return sqlAppender.toString();
    }

    public String buildSelectFields(Map<String, Object> fieldConfig, String tableName, SqlParam param) {
        if (param == null) return "*";
        Set<String> includes = param.getIncludes(),
                excludes = param.getExcludes();
        boolean includesIsEmpty = includes.isEmpty(),
                excludesIsEmpty = excludes.isEmpty();
        if (includesIsEmpty && excludesIsEmpty)
            return "*";
        Map<String, String> propertyMapper = getPropertyMapper(fieldConfig, param);
        SqlAppender appender = new SqlAppender();
        propertyMapper.forEach((k, v) -> {
            if (!appender.isEmpty())
                appender.add(",");
            appender.add(tableName, ".", k, " as ").addEdSpc(getQuoteStart(), k, getQuoteEnd());
        });
        if (appender.isEmpty()) return "*";
        return appender.toString();
    }

    public String buildUpdateFields(Map<String, Object> fieldConfig, UpdateParam param) throws Exception {
        Map<String, String> propertyMapper = getPropertyMapper(fieldConfig, param);
        SqlAppender appender = new SqlAppender();
        propertyMapper.forEach((k, v) -> {
            try {
                Object obj = BeanUtils.getProperty(param.getData(), v);
                if (obj != null) {
                    if (!appender.isEmpty())
                        appender.add(",");
                    Map<String, Object> config = ((Map) fieldConfig.get(k));
                    appender.add(k, "=", "#{data.", v);
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

    public String buildOrder(Map<String, Object> fieldConfig, String tableName, QueryParam param) throws Exception {
        QueryParam tmp = new QueryParam();
        tmp.setSorts(param.getSorts());
        Map<String, String> propertyMapper = getPropertyMapper(fieldConfig, tmp);
        if (tmp.getSorts().isEmpty()) return "";
        Set<Sort> sorts = new LinkedHashSet<>();
        param.getSorts().forEach(sort -> {
            String fieldName = sort.getField();
            if (StringUtils.isNullOrEmpty(fieldName)) return;
            if (fieldName.contains("."))
                fieldName = fieldName.split("[.]")[1];
            if (propertyMapper.containsKey(fieldName) || propertyMapper.containsValue(fieldName)) {
                sorts.add(sort);
            }
        });
        if (sorts.isEmpty()) return "";
        String sql = sorts.stream()
                .map(sort -> {
                    String fieldName = sort.getField();
                    if (fieldName.contains("."))
                        fieldName = fieldName.split("[.]")[1];
                    return new SqlAppender()
                            .add(tableName, ".", StringUtils.camelCase2UnderScoreCase(fieldName), " ", sort.getDir()).toString();
                })
                .reduce((s, s1) -> new SqlAppender().add(s, ",", s1).toString()).get();
        return " order by ".concat(sql);
    }

    public Map<String, String> getPropertyMapper(Map<String, Object> fieldConfig, SqlParam param) {
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
            String column = getColumn(fieldConfig, term.getField());
            if (column != null) term.setField(column);
            boolean nullTerm = StringUtils.isNullOrEmpty(term.getField());
            //不是空条件 也不是可选字段
            if (!nullTerm && !fieldConfig.containsKey(term.getField())) continue;
            //不是空条件，值为空
            if (!nullTerm && StringUtils.isNullOrEmpty(term.getValue())) continue;
            //是空条件，但是无嵌套
            if (nullTerm && term.getTerms().isEmpty()) continue;
            //用于sql预编译的参数名
            prefix = StringUtils.concat(prefixTmp, "terms[", index++, "]");
            //JDBC类型
            JDBCType jdbcType = getFieldJDBCType(term.getField(), fieldConfig);
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
                    appender.add(mapperMap.get(term.getTermType()).fieldMapper(prefix + ".value", tableName, term, jdbcType));
                appender.addAll(nest);
                appender.add(")");
            } else {
                if (!nullTerm)
                    appender.add(mapperMap.get(term.getTermType()).fieldMapper("" + prefix + ".value", tableName, term, jdbcType));
            }
        }
    }

    protected Object transformationValue(JDBCType type, Object value) {
        switch (type) {
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

    protected List<Object> param2list(Object value) {
        if (value == null) return new ArrayList<>();
        if (!(value instanceof Iterable)) {
            if (value instanceof String) {
                String[] arr = ((String) value).split("[, ;]");
                Object[] objArr = new Object[arr.length];
                for (int i = 0; i < arr.length; i++) {
                    String str = arr[i];
                    Object val = str;
                    if (StringUtils.isInt(str))
                        val = StringUtils.toInt(str);
                    else if (StringUtils.isDouble(str))
                        val = StringUtils.toDouble(str);
                    objArr[i] = val;
                }
                return Arrays.asList(objArr);
            } else if (value.getClass().isArray()) {
                return Arrays.asList(((Object[]) value));
            } else {
                return Arrays.asList(value);
            }
        }
        return new ArrayList<>();
    }

    public interface KeyWordMapper {
        String fieldMapper(String paramKey, String tableName, Term term, JDBCType jdbcType);
    }
}
