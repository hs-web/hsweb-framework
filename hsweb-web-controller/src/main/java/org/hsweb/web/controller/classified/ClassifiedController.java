package org.hsweb.web.controller.classified;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.classified.Classified;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.service.classified.ClassifiedService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/classified")
@Authorize(module = "classified")
public class ClassifiedController extends GenericController<Classified, String> {

    //默认服务类
    @Resource
    private ClassifiedService classifiedService;

    @Override
    public ClassifiedService getService() {
        return this.classifiedService;
    }

    @RequestMapping(value = "/byType/{type}")
    public ResponseMessage listByType(@PathVariable("type") String type, QueryParam param) throws Exception {
        param.where("type", type);
        return list(param);
    }
}
