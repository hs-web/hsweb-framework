package org.hswebframework.web.workflow.flowable.service.imp;

import org.hswebframework.web.entity.organizational.PersonEntity;
import org.hswebframework.web.entity.workflow.ActDefEntity;
import org.hswebframework.web.organizational.authorization.relation.Relation;
import org.hswebframework.web.service.organizational.PersonService;
import org.hswebframework.web.service.organizational.RelationDefineService;
import org.hswebframework.web.service.organizational.RelationInfoService;
import org.hswebframework.web.service.workflow.ActDefService;
import org.hswebframework.web.workflow.flowable.service.BpmActivityService;
import org.hswebframework.web.workflow.flowable.service.BpmUtilsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author wangwei
 * @Date 2017/9/13.
 */
@Service
@Transactional(rollbackFor = Throwable.class)
public class BpmUtilsServiceImp implements BpmUtilsService {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    PersonService personService;
    @Autowired
    RelationInfoService relationInfoService;

    @Override
    public List<String> selectUserIdsBy(String userId, ActDefEntity actDefEntity) {
        List<String> list = new ArrayList<>();
        // 根据配置类型  获取人员信息 设置待办人
        if (actDefEntity.getType().equals("person")) { // 矩阵
            List<Relation> relations = relationInfoService.getRelations(actDefEntity.getType(), userId).findPos(actDefEntity.getDefId());
            for (Relation relation : relations) {
                list.add(relation.getTarget());
            }
        } else if (actDefEntity.getType().equals("position")) { // 岗位
            List<PersonEntity> personEntities = personService.selectByPositionId(actDefEntity.getDefId());
            for(PersonEntity personEntity:personEntities){
                list.add(personEntity.getUserId());
            }
        } else if (actDefEntity.getType().equals("role")) {  // 角色
            List<PersonEntity> personEntities = personService.selectByRoleId(actDefEntity.getDefId());
            for(PersonEntity personEntity:personEntities){
                list.add(personEntity.getUserId());
            }
        }
        return list;
    }
}
