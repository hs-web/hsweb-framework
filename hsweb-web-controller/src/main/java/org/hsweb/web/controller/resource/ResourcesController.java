package org.hsweb.web.controller.resource;

import org.hsweb.web.exception.BusinessException;
import org.hsweb.web.exception.NotFoundException;
import org.hsweb.web.logger.annotation.AccessLogger;
import org.hsweb.web.authorize.annotation.Authorize;
import org.hsweb.web.bean.po.resource.Resources;
import org.hsweb.web.bean.po.role.Role;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.message.ResponseMessage;
import org.hsweb.web.service.resource.ResourcesService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 资源控制器，继承自GenericController,使用rest+json
 * Created by generator 2015-8-28 13:01:29
 *
 * @author 浩
 * @version 1.0
 * @UpdateRemark 2015年8月31日，重写{@link ResourcesController#delete(String)}方法，删除资源时需要授权认证
 */
@RestController
@RequestMapping(value = "/resources")
@AccessLogger("资源管理")
public class ResourcesController extends GenericController<Resources, String> {

    //默认服务类
    @Resource
    private ResourcesService resourcesService;

    @Override
    public ResourcesService getService() {
        return this.resourcesService;
    }

    /**
     * 重写 {@link GenericController#delete(Object)} 新增注解: @Authorize(role = Role.SYS_ROLE_ADMIN),只有拥有系统管理员权限的用户才能执行此操作
     *
     * @param id 要删除的id标识
     * @return
     */
    @Override
    @Authorize(role = Role.SYS_ROLE_ADMIN)
    public ResponseMessage delete(@PathVariable("id") String id) throws Exception {
        return super.delete(id);
    }

    /**
     * 判断资源文件是否存在并检测其健康状况
     *
     * @param id 资源文件id
     * @return 查询结果
     */
    @RequestMapping(value = "/{id:^[0-9a-zA-Z]*$}", method = RequestMethod.GET)
    @ResponseBody
    @AccessLogger("获取资源信息")
    public ResponseMessage info(@PathVariable("id") String id) throws Exception {
        Resources resources;
        //如果id长度为32，则尝试通过md5获取
        if (id.length() == 32) {
            resources = getService().selectByMd5(id);
            if (resources == null)
                resources = getService().selectByPk(id);
        } else
            resources = resourcesService.selectByPk(id);
        if (resources == null) {
            throw new NotFoundException("资源不存在");
        } else {
            if (resources.getStatus() != 1)
                throw new NotFoundException("资源不存在,或不可用！");
            return ResponseMessage.ok(resources);
        }
    }


}
