package org.hswebframework.web.crud.query;

import org.hswebframework.ezorm.core.Conditional;
import org.hswebframework.ezorm.core.StaticMethodReferenceColumn;

public interface JoinConditionalSpec<C extends JoinConditionalSpec<C>> extends JoinOnSpec<C>, Conditional<C> {

    @Override
    JoinNestConditionalSpec<C> nest();

    @Override
    JoinNestConditionalSpec<C> orNest();

    /**
     * 使用方法引用定义join表别名。
     *
     * <pre>{@code
     * // join t_detail detail ....
     *  alias(MyEntity.getDetail)
     * }</pre>
     *
     * @param alias 别名
     * @return this
     */
    default <T> C alias(StaticMethodReferenceColumn<T> alias) {
        return alias(alias.getColumn());
    }

    /**
     * 定义join表别名，在后续列转换和条件中可以使用别名进行引用。
     *
     * @param alias 别名
     * @return this
     */
    C alias(String alias);


}
