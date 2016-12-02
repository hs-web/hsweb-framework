package org.hsweb.web.service;

import org.hsweb.ezorm.core.dsl.Update;
import org.hsweb.web.bean.common.UpdateParam;
import org.hsweb.web.bean.po.GenericPo;
import org.hsweb.web.dao.GenericMapper;
import org.hsweb.web.dao.UpdateMapper;

import java.util.HashMap;
import java.util.List;

public interface UpdateService<Po> {
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
     * 指定一个dao映射接口,接口需继承{@link GenericMapper}创建dsl数据更新操作对象<br>
     * 可通过返回的Update对象进行dsl方式操作如:<br>
     * <code>
     * createUpdate(userMapper).where("id",1).exec();
     * </code>
     *
     * @param mapper dao映射接口
     * @param <PO>   PO泛型
     * @return {@link Update}
     * @see Update
     * @see org.hsweb.ezorm.core.Conditional
     * @see UpdateParam
     * @since 2.2
     */
    static <PO> Update<PO, UpdateParam<PO>> createUpdate(UpdateMapper<PO> mapper) {
        return Update.build(mapper::update, new UpdateParam(new HashMap<>()));
    }

}
