package org.hsweb.web.service.impl.classified;

import org.hsweb.web.bean.po.classified.Classified;
import org.hsweb.web.dao.classified.ClassifiedMapper;
import org.hsweb.web.service.classified.ClassifiedService;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 数据分类服务类
 * Created by generator
 */
@Service("classifiedService")
public class ClassifiedServiceImpl extends AbstractServiceImpl<Classified, String> implements ClassifiedService {

    //默认数据映射接口
    @Resource
    protected ClassifiedMapper classifiedMapper;

    @Override
    protected ClassifiedMapper getMapper() {
        return this.classifiedMapper;
    }

}
