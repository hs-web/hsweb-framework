package org.hswebframework.web.dao.mybatis.mapper.h2;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.dialect.Dialect;
import org.hswebframework.web.dao.mybatis.mapper.EnumDicInTermTypeMapper;

public class H2EnumDicInTermTypeMapper extends EnumDicInTermTypeMapper {

    public H2EnumDicInTermTypeMapper(boolean not) {
        this(not, false);
    }

    public H2EnumDicInTermTypeMapper(boolean not, boolean anyIn) {
        super(Dialect.H2, not,anyIn);
    }

    @Override
    protected SqlAppender build(String wherePrefix, Term term, RDBColumnMetaData column, String tableAlias) {
        String columnName = dialect.buildColumnName(tableAlias, column.getName());
        String where = "#{" + wherePrefix + ".value}";
        return new SqlAppender()
                .add("BITAND(", columnName, ",", where, ")", not ? "!=" : "=", anyIn ? "0" : where);
    }
}
