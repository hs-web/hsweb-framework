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
import org.hswebframework.web.commons.entity.TreeSupportEntity;
import org.hswebframework.web.dao.dynamic.QueryByEntityDao;
import org.hswebframework.web.dao.organizational.*;
import org.hswebframework.web.entity.organizational.*;
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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    @Autowired
    private DepartmentDao departmentDao;

    @Autowired
    private OrganizationalDao organizationalDao;

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
        //获取所有职位,并得到根职位(树结构)
        List<PositionEntity> positionEntities = getAllChildrenAndReturnRootNode(positionDao, positionIds, PositionEntity::setChildren, rootPosList -> {
            //根据职位获取部门
            Set<String> departmentIds = rootPosList.stream().map(PositionEntity::getDepartmentId).collect(Collectors.toSet());
            if (null != departmentIds && !departmentIds.isEmpty()) {
                List<DepartmentEntity> departmentEntities = getAllChildrenAndReturnRootNode(departmentDao, departmentIds, DepartmentEntity::setChildren, rootDepList -> {
                    //根据部门获取机构
                    Set<String> orgIds = rootDepList.stream().map(DepartmentEntity::getOrgId).collect(Collectors.toSet());
                    if (null != orgIds && !orgIds.isEmpty()) {
                        List<OrganizationalEntity> orgEntities = getAllChildrenAndReturnRootNode(organizationalDao, orgIds, OrganizationalEntity::setChildren, rootOrgList -> {
                            //根据机构获取地区
                            // TODO: 17-5-25
                        });
                        authorization.setOrgIds(transformationTreeNode(null, orgEntities));
                    }
                });
                authorization.setDepartmentIds(transformationTreeNode(null, departmentEntities));
            }
        });
        authorization.setPositionIds(transformationTreeNode(null, positionEntities));
        return authorization;
    }

    /**
     * 获取一个树形结构的数据,并返回根节点集合
     *
     * @param dao           查询dao接口
     * @param rootIds       根节点ID集合
     * @param childAccepter 子节点接收方法
     * @param rootConsumer  根节点消费回调
     * @param <T>           节点类型
     * @return 根节点集合
     */
    protected <T extends TreeSupportEntity<String>> List<T> getAllChildrenAndReturnRootNode(QueryByEntityDao<T> dao,
                                                                                            Set<String> rootIds,
                                                                                            BiConsumer<T, List<T>> childAccepter,
                                                                                            Consumer<List<T>> rootConsumer) {
        //获取根节点
        List<T> root = DefaultDSLQueryService.createQuery(dao)
                .where().in(TreeSupportEntity.id, rootIds)
                .list();
        //节点不存在?
        if (!root.isEmpty()) {
            //所有子节点,使用节点的path属性进行快速查询,查询结果包含了根节点
            List<T> allNode = DefaultDSLQueryService
                    .createQuery(dao)
                    //遍历生成查询条件: like path like ?||'%' or path like ?||'%'  ....
                    .each(root, (query, data) -> query.or().like$(TreeSupportEntity.path, data.getPath()))
                    .list();
            //转为树形结构
            List<T> tree = TreeSupportEntity
                    .list2tree(allNode, childAccepter,
                            (Predicate<T>) node -> rootIds.contains(node.getId()));  // 根节点判定
            rootConsumer.accept(root);
            return tree;
        }
        return Collections.emptyList();
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
        PersonEntity entity = createQuery().where(PersonEntity.userId, userId).single();
        assertNotNull(entity);
        return getPersonnelAuthorizationByPersonId(entity.getId());
    }
}
