package org.hsweb.web.mybatis.builder;

import org.hsweb.web.bean.common.Term;
import org.hsweb.web.bean.common.TermType;
import org.hsweb.web.mybatis.utils.SqlAppender;
import org.webbuilder.utils.common.DateTimeUtils;
import org.webbuilder.utils.common.StringUtils;

import java.sql.JDBCType;
import java.util.*;

/**
 * Created by zhouhao on 16-5-9.
 */
public class DefaultSqlParamBuilder {

    protected Map<TermType, KeyWordMapper> mapperMap = new HashMap<>();

    private static DefaultSqlParamBuilder instance = new DefaultSqlParamBuilder();

    public DefaultSqlParamBuilder() {
        mapperMap.put(TermType.eq, (paramKey, field, jdbcType) ->
                        new SqlAppender().add(field.getField(), " = ", "#{", paramKey, "}").toString()
        );
        mapperMap.put(TermType.not, (paramKey, field, jdbcType) ->
                        new SqlAppender().add(field.getField(), " != ", "#{", paramKey, "}").toString()
        );
        mapperMap.put(TermType.like, (paramKey, field, jdbcType) ->
                        new SqlAppender().add(field.getField(), " like ", "#{", paramKey, "}").toString()
        );
        mapperMap.put(TermType.notlike, (paramKey, field, jdbcType) ->
                        new SqlAppender().add(field.getField(), " not like ", "#{", paramKey, "}").toString()
        );
        mapperMap.put(TermType.notnull, (paramKey, field, jdbcType) ->
                        new SqlAppender().add(field.getField(), " is not null").toString()
        );
        mapperMap.put(TermType.isnull, (paramKey, field, jdbcType) ->
                        new SqlAppender().add(field.getField(), " is null").toString()
        );
        mapperMap.put(TermType.empty, (paramKey, field, jdbcType) ->
                        new SqlAppender().add(field.getField(), " =''").toString()
        );
        mapperMap.put(TermType.notempty, (paramKey, field, jdbcType) ->
                        new SqlAppender().add(field.getField(), " !=''").toString()
        );
        mapperMap.put(TermType.btw, (paramKey, field, jdbcType) ->
        {
            SqlAppender sqlAppender = new SqlAppender();
            List<Object> objects = param2list(field.getValue());
            if (objects.size() == 1)
                objects.add(objects.get(0));
            field.setValue(objects);
            sqlAppender.addSpc(field.getField(), "between")
                    .add(" #{", paramKey, "[0]}")
                    .add(" and ", "#{", paramKey, "[1]}");
            return sqlAppender.toString();
        });

        mapperMap.put(TermType.notbtw, (paramKey, field, jdbcType) ->
        {
            SqlAppender sqlAppender = new SqlAppender();
            List<Object> objects = param2list(field.getValue());
            if (objects.size() == 1)
                objects.add(objects.get(0));
            field.setValue(objects);
            sqlAppender.addSpc(field.getField(), "not between")
                    .add(" #{", paramKey, "[0]}")
                    .add(" and ", "#{", paramKey, "[1]}");
            return sqlAppender.toString();
        });

        mapperMap.put(TermType.gt, (paramKey, field, jdbcType) -> {
            SqlAppender sqlAppender = new SqlAppender();
            if (Arrays.<JDBCType>asList(JDBCType.DATE, JDBCType.TIME, JDBCType.TIMESTAMP).contains(jdbcType)) {
                if (!(field.getValue() instanceof Date)) {
                    String strValue = String.valueOf(field.getValue());
                    Date date = DateTimeUtils.formatUnknownString2Date(strValue);
                    if (date != null) field.setValue(date);
                }
            }
            sqlAppender.add(field.getField(), " >= #{", paramKey, "}");
            return sqlAppender.toString();
        });

        mapperMap.put(TermType.lt, (paramKey, field, jdbcType) -> {
            SqlAppender sqlAppender = new SqlAppender();
            if (Arrays.<JDBCType>asList(JDBCType.DATE, JDBCType.TIME, JDBCType.TIMESTAMP).contains(jdbcType)) {
                if (!(field.getValue() instanceof Date)) {
                    String strValue = String.valueOf(field.getValue());
                    Date date = DateTimeUtils.formatUnknownString2Date(strValue);
                    if (date != null) field.setValue(date);
                }
            }
            sqlAppender.add(field.getField(), " <= #{", paramKey, "}");
            return sqlAppender.toString();
        });

        mapperMap.put(TermType.in, (paramKey, field, jdbcType) -> {
            List<Object> values = param2list(field.getValue());
            field.setValue(values);
            SqlAppender appender = new SqlAppender();
            appender.addSpc(field.getField(), "in(");
            for (int i = 0; i < values.size(); i++) {
                appender.add("#{", paramKey, "[", i, "]}", ",");
            }
            appender.removeLast();
            appender.add(")");
            return appender.toString();
        });

        mapperMap.put(TermType.notin, (paramKey, field, jdbcType) -> {
            List<Object> values = param2list(field.getValue());
            field.setValue(values);
            SqlAppender appender = new SqlAppender();
            appender.addSpc(field.getField(), "not in(");
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

    public String buildWhere(Map<String, Object> fieldConfig, List<Term> terms) {
        SqlAppender sqlAppender = new SqlAppender();
        buildWhere(fieldConfig, "", terms, sqlAppender);
        if (sqlAppender.size() > 0) sqlAppender.removeFirst();
        return sqlAppender.toString();
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

    public void buildWhere(Map<String, Object> fieldConfig, String prefix, List<Term> terms, SqlAppender appender) {
        if (terms == null || terms.isEmpty()) return;
        int index = 0;
        String prefixTmp = StringUtils.concat(prefix, StringUtils.isNullOrEmpty(prefix) ? "" : ".");
        for (Term term : terms) {
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
                buildWhere(fieldConfig, prefix, term.getTerms(), nest);
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
                    appender.add(mapperMap.get(term.getTermType()).fieldMapper(prefix + ".value", term, jdbcType));
                appender.addAll(nest);
                appender.add(")");
            } else {
                if (!nullTerm)
                    appender.add(mapperMap.get(term.getTermType()).fieldMapper("" + prefix + ".value", term, jdbcType));
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
        String fieldMapper(String paramKey, Term term, JDBCType jdbcType);
    }
}
