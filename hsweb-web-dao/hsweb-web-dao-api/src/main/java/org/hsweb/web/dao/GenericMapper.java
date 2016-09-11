package org.hsweb.web.dao;

import org.hsweb.web.bean.common.DeleteParam;
import org.hsweb.web.bean.common.InsertParam;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.common.UpdateParam;

import java.util.List;

/**
 * 通用dao，定义常用的增删改查操作。其他daoMapper接口继承此接口，则无需再定义这些方法
 * <p>
 * Created by zh.sqy@qq.com on 2015-07-20 0020.
 */
public interface GenericMapper<Po, Pk> {
    /**
     * 根据参数添加一条数据
     *
     * @param param 参数对象
     * @return 添加后生成的主键
     */
    int insert(InsertParam<Po> param);

    /**
     * 根据条件删除数据
     *
     * @param param 主键
     * @return 影响记录数
     */
    int delete(DeleteParam param);

    /**
     * 修改记录信息
     *
     * @param data 要修改的对象
     * @return 影响记录数
     */
    int update(UpdateParam<Po> data);

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
     * 查询记录总数，用于分页等操作。查询条件同 {@link GenericMapper#select}
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
}
