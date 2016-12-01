package org.hsweb.web.service;

import org.hsweb.ezorm.core.dsl.Delete;
import org.hsweb.web.bean.common.DeleteParam;
import org.hsweb.web.dao.DeleteMapper;
import org.hsweb.web.dao.GenericMapper;

/**
 * @author zhouhao
 */
public interface DeleteService<Pk> {
    /**
     * 根据主键删除记录
     *
     * @param pk 主键
     * @return 影响记录数
     */
    int delete(Pk pk);

    Delete createDelete();

    /**
     * 指定一个dao映射接口,接口需继承{@link GenericMapper}创建dsl数据删除操作对象<br>
     * 可通过返回的Update对象进行dsl方式操作如:<br>
     * <code>
     * createDelete(userMapper).where("id",1).exec();
     * </code>
     *
     * @param mapper dao映射结构
     * @return {@link Delete}
     * @see Delete
     * @see org.hsweb.ezorm.core.Conditional
     * @since 2.2
     */
    static Delete createDelete(DeleteMapper mapper) {
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

}
