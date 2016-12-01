package org.hsweb.web.service;

import org.hsweb.ezorm.core.dsl.Query;
import org.hsweb.web.bean.common.PagerResult;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.dao.GenericMapper;

import java.util.List;

public interface QueryService<Po, Pk> {
    /**
     * 分页进行查询数据，查询条件同 {@link GenericService#select}
     *
     * @param param 查询参数
     * @return 分页结果
     * @ 查询异常
     */
    PagerResult<Po> selectPager(QueryParam param);

    /**
     * 根据查询参数进行查询，参数可使用 {@link Query}进行构建
     * 推荐使用 {@link this#createQuery()}进行查询
     *
     * @param param 查询参数
     * @return 查询结果
     * @see Query
     */
    List<Po> select(QueryParam param);

    List<Po> select();

    /**
     * 查询记录总数，用于分页等操作。查询条件同 {@link GenericService#select}
     *
     * @param param 查询参数
     * @return 查询结果，实现mapper中的sql应指定默认值，否则可能抛出异常
     */
    int total(QueryParam param);

    /**
     * 根据主键查询记录
     *
     * @param pk 主键
     * @return 查询结果
     */
    Po selectByPk(Pk pk);

    /**
     * 查询只返回单个结果
     *
     * @param param 查询条件
     * @return 单个结果
     * @see this#select(QueryParam)
     */
    default Po selectSingle(QueryParam param) {
        param.doPaging(0, 1);
        List<Po> list = this.select(param);
        if (list.size() == 0) return null;
        else return list.get(0);
    }

    /**
     * 创建本服务的dsl查询操作对象
     * 可通过返回的Query对象进行dsl方式操作如:<br>
     * <code>
     * createQuery().where("id",1).single();
     * </code>
     *
     * @return {@link Query}
     * @see Query
     * @see org.hsweb.ezorm.core.Conditional
     * @since 2.2
     */
    default Query<Po, QueryParam> createQuery() {
        Query<Po, QueryParam> query = Query.empty(new QueryParam());
        query.setListExecutor(this::select);
        query.setTotalExecutor(this::total);
        query.setSingleExecutor(this::selectSingle);
        return query;
    }

    /**
     * 指定一个dao映射接口,接口需继承{@link GenericMapper}创建dsl数据查询对象<br>
     * 可通过返回的Query对象进行dsl方式操作如:<br>
     * <code>
     * createQuery(userMapper).where("id",1).single();
     * </code>
     *
     * @param mapper dao映射结构
     * @param <PO>   PO泛型
     * @param <PK>   主键泛型
     * @return {@link Query}
     * @see Query
     * @see org.hsweb.ezorm.core.Conditional
     * @since 2.2
     */
    static <PO, PK> Query<PO, QueryParam> createQuery(GenericMapper<PO, PK> mapper) {
        Query<PO, QueryParam> query = new Query<>(new QueryParam());
        query.setListExecutor(mapper::select);
        query.setTotalExecutor(mapper::total);
        query.setSingleExecutor((param) -> {
            param.doPaging(0, 1);
            List<PO> list = mapper.select(param);
            if (null == list || list.size() == 0) return null;
            else return list.get(0);
        });
        return query;
    }
}
