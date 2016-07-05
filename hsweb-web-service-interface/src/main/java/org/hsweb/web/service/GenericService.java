package org.hsweb.web.service;

import org.hsweb.web.bean.common.PagerResult;
import org.hsweb.web.bean.common.QueryParam;

import java.util.List;


/**
 * 通用Service。继承了通用mapper的常用增删改查方法
 * <p>
 * Created by zh.sqy@qq.com on 2015-07-20 0020.
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

    default List<Pk> batchInsert(List<Po> data) {
        return batchInsert(data, false);
    }

    /**
     * 批量添加数据
     *
     * @param data     数据集合
     * @param skipFail 是否跳过验证失败的的数据
     * @return 添加后产生的主键集合
     */
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

    int saveOrUpdate(Po po);

    /**
     * 根据条件集合查询记录，支持分页，排序。
     * <br/>查询条件支持 类似$LIKE,$IN 表达式查询，如传入 name$LIKE 则进行name字段模糊查询
     * <br/>$LIKE -->模糊查询 (只支持字符)
     * <br/>$START -->以?开始 (只支持字符 和数字)
     * <br/>$END -->以?结尾 (只支持字符 和数字)
     * <br/>$IN -->in查询，参数必须为List实现，传入类似 1,2,3 是非法的
     * <br/>$GT -->大于 (只支持 数字和日期)
     * <br/>$LT -->小于 (只支持 数字和日期)
     * <br/>$NOT -->不等于
     * <br/>$NOTNULL -->值不为空
     * <br/>$ISNULL -->值为空
     * <br/>所有操作支持取反
     *
     * @param param 查询参数
     * @return 查询结果
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
     * @
     */
    default Po selectSingle(QueryParam param) {
        param.doPaging(0, 1);
        List<Po> list = this.select(param);
        if (list.size() == 0) return null;
        else return list.get(0);
    }

}
