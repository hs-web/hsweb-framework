package org.hsweb.web.controller.classified;

import org.hsweb.web.bean.po.classified.Classified;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.service.classified.ClassifiedService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
* 控制器，继承自GenericController,使用rest+json
* Created by hsweb-generator 2016-5-14 10:18:41
*/
@RestController
@RequestMapping(value = "/classified")
public class ClassifiedController extends GenericController<Classified,String> {

    //默认服务类
    @Resource
    private ClassifiedService classifiedService;

    @Override
    public ClassifiedService getService(){
        return this.classifiedService;
    }

}
