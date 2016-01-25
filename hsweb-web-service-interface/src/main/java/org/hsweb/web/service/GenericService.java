package org.hsweb.web.service;

import org.hsweb.web.bean.common.PagerResult;
import org.hsweb.web.bean.common.QueryParam;

import java.util.List;


/**
 * 通用Service。继承了通用mapper的常用增删改查方法
 * <p/>
 * Created by zh.sqy@qq.com on 2015-07-20 0020.
 */
public interface GenericService<Po, Pk> {

    /**
     * 分页进行查询数据，查询条件同 {@link GenericService#select}
     *
     * @param param 查询参数
     * @return 分页结果
     * @throws Exception 查询异常
     */
    PagerResult<Po> selectPager(QueryParam param) throws Exception;

    /**
     * 添加一条数据
     *
     * @param data 要添加的数据
     * @return 添加后生成的主键
     * @throws Exception 异常信息
     */
    Pk insert(Po data) throws Exception;

    /**
     * 根据主键删除记录
     *
     * @param pk 主键
     * @return 影响记录数
     * @throws Exception 异常信息
     */
    int delete(Pk pk) throws Exception;

    /**
     * 修改记录信息
     *
     * @param data 要修改的对象
     * @return 影响记录数
     * @throws Exception 异常信息
     */
    int update(Po data) throws Exception;

    /**
     * 批量修改记录
     *
     * @param data 要修改的记录集合
     * @return 影响记录数
     * @throws Exception 异常信息
     */
    int update(List<Po> data) throws Exception;

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
     * @throws Exception 异常信息
     */
    List<Po> select(QueryParam param) throws Exception;

    /**
     * 查询记录总数，用于分页等操作。查询条件同 {@link GenericService#select}
     *
     * @param param 查询参数
     * @return 查询结果，实现mapper中的sql应指定默认值，否则可能抛出异常
     * @throws Exception 异常信息
     */
    int total(QueryParam param) throws Exception;

    /**
     * 根据主键查询记录
     *
     * @param pk 主键
     * @return 查询结果
     * @throws Exception 异常信息
     */
    Po selectByPk(Pk pk) throws Exception;

}
