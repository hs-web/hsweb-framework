package org.hswebframework.web.commons.beans.param;

import org.hsweb.ezorm.core.param.QueryParam;
import org.hswebframework.web.commons.beans.Bean;

/**
 * 查询参数实体,使用<a href="https://github.com/hs-web/hsweb-easy-orm">easyorm</a>进行动态查询参数构建<br/>
 * 可通过静态方法创建:<br/>
 * {@link QueryParamBean#empty()}<br/>
 * {@link QueryParamBean#single(String, Object)}<br/>
 * 如:
 * <code>
 * QueryParamBean.single("id",id);
 * </code>
 *
 * @author zhouhao
 * @see QueryParam
 * @see Bean
 * @since 3.0
 */
public class QueryParamBean extends QueryParam implements Bean {

    /**
     * 创建一个空的查询参数实体,该实体无任何参数.
     *
     * @return 无条件的参数实体
     */
    public static QueryParamBean empty() {
        return new QueryParamBean();
    }

    /**
     * 创建一个含有单个条件的参数实体,条件默认为is
     *
     * @param field 参数名称
     * @param value 参数值
     * @return 单个条件的参数实体
     * @see QueryParam#where(String, Object)
     */
    public static QueryParamBean single(String field, Object value) {
        return empty().where(field, value);
    }
}
