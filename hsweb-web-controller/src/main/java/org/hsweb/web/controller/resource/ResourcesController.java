package org.hsweb.web.controller.resource;

import org.hsweb.web.bean.po.resource.Resources;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.service.resource.ResourcesService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/resources")
@AccessLogger("资源管理")
@Authorize(module = "resources")
public class ResourcesController extends GenericController<Resources, String> {

    //默认服务类
    @Resource
    private ResourcesService resourcesService;

    @Override
    public ResourcesService getService() {
        return this.resourcesService;
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
    public ResponseMessage info(@PathVariable("id") String id) {
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
