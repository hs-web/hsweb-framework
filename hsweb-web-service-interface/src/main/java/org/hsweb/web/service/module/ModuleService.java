package org.hsweb.web.service.module;

import org.hsweb.web.bean.po.module.Module;
import org.hsweb.web.service.GenericService;

import java.util.List;


/**
 * 系统模块服务类
 * Created by generator
 */
public interface ModuleService extends GenericService<Module, String> {
    /**
     * 根据父级ID查询所有子级的模块
     *
     * @param pid 父级ID
     * @return 子级的模块集合
     * @throws Exception 查询异常
     */
    List<Module> selectByPid(String pid) throws Exception;
}
