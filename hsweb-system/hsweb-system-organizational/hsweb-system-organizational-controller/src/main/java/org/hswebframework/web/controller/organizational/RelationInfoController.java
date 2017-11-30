package org.hswebframework.web.controller.organizational;

import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.entity.organizational.RelationInfoEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.organizational.RelationInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 关系信息
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.relationInfo:relation/info}")
@Authorize(permission = "relation-info", description = "关系管理")
@Api(value = "关系管理",tags = "组织架构-关系管理")
public class RelationInfoController implements SimpleGenericEntityController<RelationInfoEntity, String, QueryParamEntity> {

    private RelationInfoService relationInfoService;

    @Autowired
    public void setRelationInfoService(RelationInfoService relationInfoService) {
        this.relationInfoService = relationInfoService;
    }

    @Override
    public RelationInfoService getService() {
        return relationInfoService;
    }
}
