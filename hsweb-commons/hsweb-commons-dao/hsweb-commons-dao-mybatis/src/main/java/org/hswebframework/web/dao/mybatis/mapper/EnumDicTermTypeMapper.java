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
public class EnumDicTermTypeMapper implements TermTypeMapper {

    protected Dialect dialect;

    protected boolean not;


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
            return build(wherePrefix,term,column,tableAlias);
        }
        Object value = term.getValue();
        if (type.isArray()) {
            Class componentType = type.getComponentType();
            if (support(column)) {
                EnumDict[] newValue = param2list(value)
                        .stream().map(v -> EnumDict.find(componentType, v).orElse(null))
                        .filter(Objects::nonNull)
                        .toArray(EnumDict[]::new);
                long bit = EnumDict.toMask(newValue);
                term.setValue(bit);
            }
        } else {
            if (support(column)) {
                if (value instanceof Collection) {
                    value = ((Collection) value).iterator().next();
                }
                EnumDict dict = (EnumDict) EnumDict.find(type, value).orElse(null);
                if (null != dict) {
                    term.setValue(dict.getValue());
                }
            }
        }
        return build(wherePrefix, term, column, tableAlias);
    }


    protected SqlAppender build(String wherePrefix, Term term, RDBColumnMetaData column, String tableAlias) {
        Object value = term.getValue();
        if (value instanceof Collection) {
            value = ((Collection) value).iterator().next();
            term.setValue(value);
        }
        return new SqlAppender()
                .add(dialect.buildColumnName(tableAlias, column.getName()), not ? "!=" : "=", "#{", wherePrefix, ".value}");
    }


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
