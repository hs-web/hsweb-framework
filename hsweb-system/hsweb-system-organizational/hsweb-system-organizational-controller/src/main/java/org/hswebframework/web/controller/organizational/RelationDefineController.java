package org.hswebframework.web.controller.organizational;

import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.entity.organizational.RelationDefineEntity;
import org.hswebframework.web.logging.AccessLogger;
import  org.hswebframework.web.service.organizational.RelationDefineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  关系定义
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.relationDefine:relation/define}")
@Authorize(permission = "relation-define")
@AccessLogger("关系定义")
public class RelationDefineController implements SimpleGenericEntityController<RelationDefineEntity, String, QueryParamEntity> {

    private RelationDefineService relationDefineService;
  
    @Autowired
    public void setRelationDefineService(RelationDefineService relationDefineService) {
        this.relationDefineService = relationDefineService;
    }
  
    @Override
    public RelationDefineService getService() {
        return relationDefineService;
    }
}
