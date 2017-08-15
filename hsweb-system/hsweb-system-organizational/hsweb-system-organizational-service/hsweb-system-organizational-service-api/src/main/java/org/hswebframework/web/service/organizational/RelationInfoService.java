package org.hswebframework.web.service.organizational;

import org.hswebframework.web.entity.organizational.RelationInfoEntity;
import org.hswebframework.web.organizational.authorization.relation.Relations;
import org.hswebframework.web.service.CrudService;


/**
 * 关系信息 服务类
 *
 * @author hsweb-generator-online
 */
public interface RelationInfoService extends CrudService<RelationInfoEntity, String> {
    Relations getRelations(String relationTypeFrom, String relationFrom);
}
