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

        mapperMap.put(TermType.notnull, (paramKey, field, jdbcType) ->
                        new SqlAppender().add(field.getField(), " not null").toString()
        );
        mapperMap.put(TermType.isnull, (paramKey, field, jdbcType) ->
                        new SqlAppender().add(field.getField(), " is null").toString()
        );

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
        String sql = buildWhere(fieldConfig, "", terms);
        return sql;
    }

    public JDBCType getFieldJDBCType(String field, Map<String, Object> fieldConfig) {
        Object conf = fieldConfig.get(field);
        if (conf instanceof Map) {
            try {
                return JDBCType.valueOf(String.valueOf(((Map) conf).get("jdbcType")));
            } catch (Exception e) {
            }
        }
        return JDBCType.VARCHAR;
    }

    public String buildWhere(Map<String, Object> fieldConfig, String prefix, List<Term> terms) {
        if (terms == null || terms.isEmpty()) return "";
        SqlAppender appender = new SqlAppender();
        int index = 0;
        String prefixTmp = StringUtils.concat(prefix, StringUtils.isNullOrEmpty(prefix) ? "" : ".");
        for (Term term : terms) {
            if (!fieldConfig.containsKey(term.getField())) continue;
            prefix = StringUtils.concat(prefixTmp, "terms[", index++, "]");
            JDBCType jdbcType = getFieldJDBCType(term.getField(), fieldConfig);
            term.setValue(transformationValue(jdbcType, term.getValue()));
            appender.addSpc(" " + term.getType().toString());
            if (term.getTerms() != null && !term.getTerms().isEmpty()) {
                appender.add("(", mapperMap.get(term.getTermType()).fieldMapper(prefix + ".value", term, jdbcType));
                appender.addSpc("", term.getType().toString())
                        .add(buildWhere(fieldConfig, prefix, term.getTerms()), ")");
            } else {
                appender.add(mapperMap.get(term.getTermType()).fieldMapper("" + prefix + ".value", term, jdbcType));
            }
        }
        appender.removeFirst();
        return appender.toString();
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
