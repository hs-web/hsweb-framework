package org.hswebframework.web.dao.mybatis.mapper;

import lombok.AllArgsConstructor;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.dialect.Dialect;
import org.hswebframework.ezorm.rdb.render.dialect.Dialect.TermTypeMapper;
import org.hswebframework.web.dict.EnumDict;

import java.util.*;

@AllArgsConstructor
@SuppressWarnings("all")
public abstract class EnumDicInTermTypeMapper implements TermTypeMapper {

    protected Dialect dialect;

    protected boolean not;

    protected boolean anyIn = false;

    protected boolean support(RDBColumnMetaData column) {
        Class type = column.getJavaType();
        if (type.isArray()) {
            type = type.getComponentType();
        }
        return type.isEnum() && EnumDict.class.isAssignableFrom(type);
    }

    @Override
    public SqlAppender accept(String wherePrefix, Term term, RDBColumnMetaData column, String tableAlias) {
        Class type = column.getJavaType();
        if(type==null){
            return buildNotSupport(wherePrefix,term,column,tableAlias);
        }
        Object value = term.getValue();
        if (type.isArray()) {
            Class componentType = type.getComponentType();
            if (support(column)) {
                if(componentType.getEnumConstants().length<64){
                    EnumDict[] newValue = param2list(value)
                            .stream().map(v -> EnumDict.find(componentType, v).orElse(null))
                            .filter(Objects::nonNull)
                            .toArray(EnumDict[]::new);
                    long bit = EnumDict.toMask(newValue);
                    term.setValue(bit);
                }else{
                    //枚举数量大于等于64,无法使用位运算
                    // TODO: 2018/4/25 尝试查询字典中间表
                    buildNotSupport(wherePrefix,term,column,tableAlias);
                }

            } else {
                return buildNotSupport(wherePrefix, term, column, tableAlias);
            }
        } else {
            //类型不是数组
//            if (support(column)) {
//                if (value instanceof Collection) {
//                    value = ((Collection) value).iterator().next();
//                }
//                EnumDict dict = value instanceof EnumDict ? (EnumDict) value : (EnumDict) EnumDict.find(type, value).orElse(null);
//                if (null != dict) {
//                    term.setValue(dict.getValue());
//                }
//            } else {
            return buildNotSupport(wherePrefix, term, column, tableAlias);
//            }
        }
        return build(wherePrefix, term, column, tableAlias);
    }

    protected SqlAppender buildNotSupport(String wherePrefix, Term term, RDBColumnMetaData column, String tableAlias) {
        List<Object> values = param2list(term.getValue());
        term.setValue(values);
        String columnName = dialect.buildColumnName(tableAlias, column.getName());
        SqlAppender appender = new SqlAppender();
        appender.add(columnName, not ? " NOT" : " ").add("IN(");
        for (int i = 0; i < values.size(); i++) {
            appender.add("#{", wherePrefix, ".value[", i, "]}", ",");
        }
        appender.removeLast();
        appender.add(")");
        return appender;
    }

    protected abstract SqlAppender build(String wherePrefix, Term term, RDBColumnMetaData column, String tableAlias);


    protected List<Object> param2list(Object value) {
        if (value == null) return new ArrayList<>();
        if (value instanceof List) return (List) value;
        if (value instanceof Collection) return new ArrayList<>(((Collection) value));
        if (value.getClass().isArray()) {
            return new ArrayList<>(Arrays.asList(((Object[]) value)));
        } else {
            return new ArrayList<>(Collections.singletonList(value));
        }
    }

}
