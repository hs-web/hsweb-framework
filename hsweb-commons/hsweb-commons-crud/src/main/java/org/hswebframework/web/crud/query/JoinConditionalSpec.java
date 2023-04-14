package org.hswebframework.web.crud.query;

import org.hswebframework.ezorm.core.Conditional;
import org.hswebframework.ezorm.core.NestConditional;
import org.hswebframework.ezorm.core.StaticMethodReferenceColumn;
import org.hswebframework.ezorm.core.param.TermType;

import java.util.function.Consumer;

public interface JoinConditionalSpec<C extends JoinConditionalSpec<C>> extends Conditional<C> {

    @Override
    JoinNestConditionalSpec<C> nest();

    @Override
    JoinNestConditionalSpec<C> orNest();

    C alias(String alias);

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
