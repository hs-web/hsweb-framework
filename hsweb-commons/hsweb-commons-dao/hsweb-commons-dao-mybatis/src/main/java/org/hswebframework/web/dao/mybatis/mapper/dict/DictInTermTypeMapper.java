package org.hswebframework.web.dao.mybatis.mapper.dict;

import org.hswebframework.ezorm.core.OptionConverter;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.core.param.TermType;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.dialect.Dialect;
import org.hswebframework.ezorm.rdb.render.dialect.RenderPhase;
import org.hswebframework.ezorm.rdb.render.dialect.function.SqlFunction;
import org.hswebframework.ezorm.rdb.render.dialect.term.BoostTermTypeMapper;
import org.hswebframework.web.dao.mybatis.mapper.AbstractSqlTermCustomizer;
import org.hswebframework.web.dao.mybatis.mapper.ChangedTermValue;
import org.hswebframework.web.dict.EnumDict;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public class DictInTermTypeMapper extends AbstractSqlTermCustomizer {

    private boolean not;

    public static final String USE_DICT_MASK_FLAG = "dict-mask";

    public DictInTermTypeMapper(boolean not) {
        super(not ? TermType.nin : TermType.in);
        this.not = not;
    }

    private boolean support(RDBColumnMetaData column) {
        if(column.getJdbcType()== JDBCType.VARCHAR){
            return false;
        }
        Class type = column.getJavaType();
        if (type != null && type.isArray()) {
            type = type.getComponentType();
        }

        return ((type != null && type.isEnum()
                && EnumDict.class.isAssignableFrom(type)
                && column.getJavaType().isArray())
                ||
                (column.getProperty(USE_DICT_MASK_FLAG).isTrue()
                        && column.getOptionConverter() != null));
    }

    @SuppressWarnings("all")
    private List<EnumDict> getAllOption(RDBColumnMetaData column) {
        Class type = column.getJavaType();
        if (null != type) {
            if (type.isArray()) {
                type = type.getComponentType();
            }
            if (type.isEnum() && EnumDict.class.isAssignableFrom(type)) {
                return (List) Arrays.asList(type.getEnumConstants());
            }
        }

        OptionConverter converter = column.getOptionConverter();
        if (converter == null) {
            return new ArrayList<>();
        }

        return (List) converter.getOptions();
    }

    @Override
    public SqlAppender accept(String wherePrefix, Term term, RDBColumnMetaData column, String tableAlias) {
        //不支持数据字典
        if (!support(column)) {
            return buildNotSupport(wherePrefix, term, column, tableAlias);
        }
        ChangedTermValue changedValue = createChangedTermValue(term);

        boolean any = term.getOptions().contains("any");

        List<Object> list = BoostTermTypeMapper.convertList(column, changedValue.getOld());

        EnumDict[] dicts = getAllOption(column)
                .stream()
                .filter(d -> d.eq(list))
                .toArray(EnumDict[]::new);

        changedValue.setValue(EnumDict.toMask(dicts));
        Dialect dialect = column.getTableMetaData().getDatabaseMetaData().getDialect();

        String columnName = dialect.buildColumnName(tableAlias, column.getName());
        String where = "#{" + wherePrefix + ".value.value}";
        SqlFunction sqlFunction = dialect.getFunction(SqlFunction.bitand);

        if (sqlFunction == null) {
            throw new UnsupportedOperationException("数据库不支持[BITAND]函数");
        }
        String bitAnd = sqlFunction.apply(SqlFunction.Param.of(RenderPhase.where, Arrays.asList(columnName, where)));

        String n;
        if (any) {
            n = not ? "=" : "!=";
        } else {
            n = not ? "!=" : "=";
        }
        return new SqlAppender().add(bitAnd, n, any ? "0" : columnName);

    }

    protected SqlAppender buildNotSupport(String wherePrefix, Term term, RDBColumnMetaData column, String tableAlias) {
        ChangedTermValue changedValue = createChangedTermValue(term);
        Dialect dialect = column.getTableMetaData().getDatabaseMetaData().getDialect();

        List<Object> values = BoostTermTypeMapper.convertList(column, changedValue.getOld());

        changedValue.setValue(values);

        String columnName = dialect.buildColumnName(tableAlias, column.getName());
        SqlAppender appender = new SqlAppender();
        appender.add(columnName, not ? " NOT " : " ").add("IN(");
        for (int i = 0; i < values.size(); i++) {
            appender.add("#{", wherePrefix, ".value.value[", i, "]}", ",");
        }
        appender.removeLast();
        appender.add(")");
        return appender;
    }
}
