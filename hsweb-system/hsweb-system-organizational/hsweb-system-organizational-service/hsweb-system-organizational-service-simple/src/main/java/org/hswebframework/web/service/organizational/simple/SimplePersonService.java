/*
 *  Copyright 2016 http://www.hswebframework.org
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package org.hswebframework.web.service.organizational.simple;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.commons.entity.TreeSortSupportEntity;
import org.hswebframework.web.commons.entity.TreeSupportEntity;
import org.hswebframework.web.dao.organizational.PersonDao;
import org.hswebframework.web.dao.organizational.PersonPositionDao;
import org.hswebframework.web.dao.organizational.PositionDao;
import org.hswebframework.web.entity.organizational.PersonEntity;
import org.hswebframework.web.entity.organizational.PersonPositionEntity;
import org.hswebframework.web.entity.organizational.PositionEntity;
import org.hswebframework.web.entity.organizational.SimplePositionEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.organizational.authorization.PersonnelAuthorization;
import org.hswebframework.web.organizational.authorization.PersonnelAuthorizationManager;
import org.hswebframework.web.organizational.authorization.TreeNode;
import org.hswebframework.web.organizational.authorization.simple.SimplePersonnel;
import org.hswebframework.web.organizational.authorization.simple.SimplePersonnelAuthorization;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.organizational.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.hswebframework.web.service.DefaultDSLQueryService.*;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("personService")
public class SimplePersonService extends GenericEntityService<PersonEntity, String>
        implements PersonService, PersonnelAuthorizationManager {
    @Autowired
    private PersonDao personDao;

    @Autowired
    private PersonPositionDao personPositionDao;

    @Autowired
    private PositionDao positionDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public PersonDao getDao() {
        return personDao;
    }

    @Override
    public String insert(PersonEntity entity) {
        return super.insert(entity);
    }

    @Override
    public PersonnelAuthorization getPersonnelAuthorizationByPersonId(String personId) {
        SimplePersonnelAuthorization authorization = new SimplePersonnelAuthorization();
        PersonEntity entity = selectByPk(personId);
        assertNotNull(entity);

        SimplePersonnel personnel = new SimplePersonnel();
        personnel.setId(entity.getId());
        personnel.setEmail(entity.getEmail());
        personnel.setName(entity.getName());
        personnel.setPhone(entity.getPhone());
        personnel.setPhoto(entity.getPhoto());
        authorization.setPersonnel(personnel);

        // 获取用户的职位ID集合(多个职位)
        Set<String> positionIds = DefaultDSLQueryService.createQuery(personPositionDao)
                .where(PersonPositionEntity.personId, personId)
                .list().stream()
                .map(PersonPositionEntity::getPositionId)
                .collect(Collectors.toSet());
        Set<String> departmentIds = null;

        if (!positionIds.isEmpty()) {
            //获取用户的职位信息
            List<PositionEntity> positions = DefaultDSLQueryService.createQuery(positionDao)
                    .where().in(PositionEntity.id, positionIds)
                    .list();
            //职位被删除了但是人员信息违背
            if (!positions.isEmpty()) {
                departmentIds = positions.stream().map(PositionEntity::getDepartmentId).collect(Collectors.toSet());
                //所有子节点,使用树节点的path属性进行快速查询
                //注意:如果path全为空,则可能导致查出全部职位
                List<PositionEntity> allPositions = DefaultDSLQueryService
                        .createQuery(positionDao)
                        //遍历生成查询条件: like path like ?||'%' or path like ?||'%'  ....
                        .each(positions, (query, position) -> query.or().like$(PositionEntity.path, position.getPath()))
                        .list();
                //转为树形结构
                List<PositionEntity> rootPositions = TreeSupportEntity
                        .list2tree(allPositions, PositionEntity::setChildren,
                                // 人员的所在职位为根节点
                                (Predicate<PositionEntity>) node -> positionIds.contains(node.getId()));
                // 转为treeNode后设置到权限信息
                authorization.setPositionIds(transformationTreeNode(null, rootPositions));
            }
            // TODO: 17-5-24 初始化部门信息
        }

        return authorization;
    }

    public static void main(String[] args) {
        String json = "[{'id':'1','name':'test','parentId':'-1'},{'id':'2','name':'test2','parentId':'-1'}" +
                ",{'id':'101','name':'test1-1','parentId':'1'},{'id':'102','name':'test1-2','parentId':'1'}]";

        List<PositionEntity> positionEntities = (List) JSON.parseArray(json, SimplePositionEntity.class);

        List<PositionEntity> rootPositions = TreeSupportEntity.list2tree(positionEntities,
                PositionEntity::setChildren,
                (Predicate<PositionEntity>) node -> "-1".equals(node.getParentId()));

        System.out.println(JSON.toJSONString(rootPositions));

        System.out.println(JSON.toJSONString(transformationTreeNode(null, rootPositions)));

    }

    public static <V extends TreeSupportEntity<String>> Set<TreeNode<String>> transformationTreeNode(V parent, List<V> data) {
        Set<TreeNode<String>> treeNodes = new HashSet<>();
        data.forEach(node -> {
            TreeNode<String> treeNode = new TreeNode<>();
            if (parent != null) {
                TreeNode<String> parentNode = new TreeNode<>();
                parentNode.setValue(parent.getId());
                parentNode.setChildren(treeNodes);
                treeNode.setParent(parentNode);
            }
            treeNode.setValue(node.getId());
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                treeNode.setChildren(transformationTreeNode(node, node.getChildren()));
            }
            treeNodes.add(treeNode);
        });
        return treeNodes;
    }

    @Override
    public PersonnelAuthorization getPersonnelAuthorizationByUserId(String userId) {
        return null;
    }
}
