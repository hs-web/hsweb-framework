package org.hswebframework.web.service.organizational.simple;

import org.hswebframework.web.dao.organizational.RelationInfoDao;
import org.hswebframework.web.entity.organizational.RelationInfoEntity;
import org.hswebframework.web.organizational.authorization.relation.*;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.organizational.RelationInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("relationInfoService")
public class SimpleRelationInfoService extends GenericEntityService<RelationInfoEntity, String>
        implements RelationInfoService {

    @Autowired
    private RelationInfoDao relationInfoDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public RelationInfoDao getDao() {
        return relationInfoDao;
    }

    @Override
    public Relations getRelations(String relationTypeFrom, String target) {
        Objects.requireNonNull(relationTypeFrom);
        Objects.requireNonNull(target);
        //获取关系
        List<RelationInfoEntity> relationInfoList = DefaultDSLQueryService.createQuery(relationInfoDao)
                //where type_from='person' and(relation_from='personId' or relation_to='personId')
                .where(RelationInfoEntity.relationTypeFrom, relationTypeFrom)
                .nest()
                .is(RelationInfoEntity.relationFrom, target)
                .or(RelationInfoEntity.relationTo, target)
                .end()
                .listNoPaging();

        List<Relation> relations = relationInfoList.stream()
                .map(info -> {
                    SimpleRelation relation = new SimpleRelation();
                    relation.setDimension(info.getRelationTypeFrom());
                    relation.setTarget(info.getRelationTo());
                    relation.setTargetObject(RelationTargetHolder
                            .get(info.getRelationTypeTo(), info.getRelationTo()).orElse(null));
                    relation.setRelation(info.getRelationId());
                    if (target.equals(info.getRelationFrom())) {
                        relation.setDirection(Relation.Direction.POSITIVE);
                    } else {
                        relation.setDirection(Relation.Direction.REVERSE);
                    }
                    return relation;
                }).collect(Collectors.toList());

        return new SimpleRelations(relations);
    }
}
