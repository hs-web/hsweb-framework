package org.hsweb.web.service;

import org.hsweb.ezorm.core.dsl.Delete;
import org.hsweb.ezorm.core.dsl.Query;
import org.hsweb.ezorm.core.dsl.Update;
import org.hsweb.ezorm.core.param.Param;
import org.hsweb.web.bean.common.*;
import org.hsweb.web.dao.GenericMapper;

import java.util.HashMap;
import java.util.List;


/**
 * 通用Service,实现增删改查
 *
 * @author zhouhao
 * @since 1.0
 */
public interface GenericService<Po, Pk> {

    /**
     * 分页进行查询数据，查询条件同 {@link GenericService#select}
     *
     * @param param 查询参数
     * @return 分页结果
     * @ 查询异常
     */
    PagerResult<Po> selectPager(QueryParam param);

    /**
     * 添加一条数据
     *
     * @param data 要添加的数据
     * @return 添加后生成的主键
     */
    Pk insert(Po data);

    /**
     * 此方法即将删除
     */
    @Deprecated
    default List<Pk> batchInsert(List<Po> data) {
        return batchInsert(data, false);
    }

    /**
     * 此方法即将删除
     */
    @Deprecated
    List<Pk> batchInsert(List<Po> data, boolean skipFail);

    /**
     * 根据主键删除记录
     *
     * @param pk 主键
     * @return 影响记录数
     */
    int delete(Pk pk);

    /**
     * 修改记录信息
     *
     * @param data 要修改的对象
     * @return 影响记录数
     */
    int update(Po data);

    /**
     * 批量修改记录
     *
     * @param data 要修改的记录集合
     * @return 影响记录数
     */
    int update(List<Po> data);

    /**
     * 保存或修改
     *
     * @param po 要修改的数据
     * @return
     */
    int saveOrUpdate(Po po);

    /**
     * 根据查询参数进行查询，参数可使用 {@link Query}进行构建
     * 推荐使用 {@link this#createQuery()}进行查询
     *
     * @param param 查询参数
     * @return 查询结果
     * @see Query
     */
    List<Po> select(QueryParam param);

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
     * 指定一个dao映射接口,接口需继承{@link GenericMapper}创建dsl数据更新操作对象<br>
     * 可通过返回的Update对象进行dsl方式操作如:<br>
     * <code>
     * createUpdate(userMapper).where("id",1).exec();
     * </code>
     *
     * @param mapper dao映射接口
     * @param <PO>   PO泛型
     * @param <PK>   主键泛型
     * @return {@link Update}
     * @see Update
     * @see org.hsweb.ezorm.core.Conditional
     * @see UpdateParam
     * @since 2.2
     */
    static <PO, PK> Update<PO, UpdateParam<PO>> createUpdate(GenericMapper<PO, PK> mapper) {
        return Update.build(mapper::update, new UpdateParam(new HashMap<>()));
    }

    /**
     * 指定一个dao映射接口,接口需继承{@link GenericMapper}创建dsl数据删除操作对象<br>
     * 可通过返回的Update对象进行dsl方式操作如:<br>
     * <code>
     * createDelete(userMapper).where("id",1).exec();
     * </code>
     *
     * @param mapper dao映射结构
     * @param <PO>   PO泛型
     * @param <PK>   主键泛型
     * @return {@link Delete}
     * @see Delete
     * @see org.hsweb.ezorm.core.Conditional
     * @since 2.2
     */
    static <PO, PK> Delete createDelete(GenericMapper<PO, PK> mapper) {
        Delete update = new Delete();
        update.setParam(new DeleteParam());
        update.setExecutor(param -> mapper.delete(((DeleteParam) param)));
        return update;
    }

    /**
     * 自定义一个删除执行器。创建dsl数据删除操作对象
     *
     * @param executor 执行器
     * @return {@link Delete}
     * @since 2.2
     */
    static Delete createDelete(Delete.Executor<DeleteParam> executor) {
        Delete update = new Delete();
        update.setParam(new DeleteParam());
        update.setExecutor(param -> executor.doExecute(((DeleteParam) param)));
        return update;
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
