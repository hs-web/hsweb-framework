package org.hswebframework.web.crud.query;

import org.hswebframework.ezorm.core.NestConditional;
import org.hswebframework.ezorm.core.StaticMethodReferenceColumn;
import org.hswebframework.ezorm.core.TermTypeConditionalSupport;
import org.hswebframework.ezorm.core.param.TermType;

public interface JoinNestConditionalSpec<C extends TermTypeConditionalSupport> extends NestConditional<C> {

    @Override
    JoinNestConditionalSpec<NestConditional<C>> nest();

    @Override
    JoinNestConditionalSpec<NestConditional<C>> orNest();

    default <T, T2> C is(StaticMethodReferenceColumn<T> mainColumn, StaticMethodReferenceColumn<T2> joinColumn) {
        return is(mainColumn.getColumn(), joinColumn);
    }

    default <T, T2> C is(String mainColumn, StaticMethodReferenceColumn<T2> joinColumn) {
        return applyColumn(mainColumn, TermType.eq, joinColumn);
    }

    default <T, T2> C is(String mainColumn, String alias, String column) {
        return applyColumn(mainColumn, TermType.eq, alias, column);
    }

    <T> C applyColumn(String mainColumn, String termType, StaticMethodReferenceColumn<T> joinColumn);

    <T> C applyColumn(String mainColumn, String termType, String alias, String column);

}
