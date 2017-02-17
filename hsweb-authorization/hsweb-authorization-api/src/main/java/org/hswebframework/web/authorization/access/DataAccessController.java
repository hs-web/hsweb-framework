package org.hswebframework.web.authorization.access;

/**
 * 数据级别权限控制器,通过此控制器对当前登录用户进行的操作进行数据级别的权限控制。
 * 如：A用户只能查询自己创建的B数据,A用户只能修改自己创建的B数据
 *
 * @author zhouhao
 * @see 3.0
 */
public interface DataAccessController {
    /**
     * 执行权限控制
     * @param access 控制方式以及配置
     * @param params 当前操作的方法的参数上下文
     * @return
     */
    boolean doAccess(DataAccess access, ParamContext params);
}
