package org.hsweb.web.service.impl.module;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.module.Module;
import org.hsweb.web.dao.module.ModuleMapper;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.hsweb.web.service.module.ModuleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("moduleService")
public class ModuleServiceImpl extends AbstractServiceImpl<Module, String> implements ModuleService {

    //默认数据映射接口
    @Resource
    protected ModuleMapper moduleMapper;

    @Override
    protected ModuleMapper getMapper() {
        return this.moduleMapper;
    }

    @Override
    public List<Module> selectByPid(String pid) throws Exception {
        return this.select(new QueryParam().where("parentId", pid));
    }
}
