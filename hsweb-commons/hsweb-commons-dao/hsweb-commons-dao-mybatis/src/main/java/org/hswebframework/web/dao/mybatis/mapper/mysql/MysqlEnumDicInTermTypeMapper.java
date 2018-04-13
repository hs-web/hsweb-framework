package org.hswebframework.web.dao.mybatis.mapper.mysql;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.dialect.Dialect;
import org.hswebframework.web.dao.mybatis.mapper.EnumDicInTermTypeMapper;

public class MysqlEnumDicInTermTypeMapper extends EnumDicInTermTypeMapper {

    public MysqlEnumDicInTermTypeMapper(boolean not) {
        super(Dialect.MYSQL, not);
    }

    @Override
    protected SqlAppender build(String wherePrefix, Term term, RDBColumnMetaData column, String tableAlias) {
        String columnName = dialect.buildColumnName(tableAlias, column.getName());
        String where = "#{" + wherePrefix + ".value}";
        return new SqlAppender()
                .add(columnName, " & ", where, not ? " != " : " = ", where);
    }


}
