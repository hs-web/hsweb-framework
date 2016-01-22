package org.hsweb.web.service.impl.module;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.module.Module;
import org.hsweb.web.dao.module.ModuleMapper;
import org.hsweb.web.exception.BusinessException;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.hsweb.web.service.module.ModuleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 系统模块服务类
 * Created by generator
 */
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
    public int update(List<Module> datas) throws Exception {
        int size = 0;
        for (Module module : datas) {
            tryValidPo(module);
            boolean doUpdate = (this.selectByPk(module.getOld_id()) != null);
            if (!module.getU_id().equals(module.getOld_id())) {
                if (doUpdate && this.selectByPk(module.getU_id()) != null) {
                    throw new BusinessException(String.format("标识:%s已存在", module.getU_id()));
                }
            }
            if (doUpdate) {
                size += this.update(module);
            } else {
                this.insert(module);
                size++;
            }
        }
        return size;
    }

    @Override
    public List<Module> selectByPid(String pid) throws Exception {
        return this.select(new QueryParam().where("p_id", pid));
    }
}
