/*
 *  Copyright 2019 http://www.hswebframework.org
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

import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.commons.entity.TreeSupportEntity;
import org.hswebframework.web.dao.dynamic.QueryByEntityDao;
import org.hswebframework.web.dao.organizational.*;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.entity.organizational.*;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.organizational.authorization.*;
import org.hswebframework.web.organizational.authorization.relation.Relation;
import org.hswebframework.web.organizational.authorization.relation.SimpleRelation;
import org.hswebframework.web.organizational.authorization.relation.SimpleRelations;
import org.hswebframework.web.organizational.authorization.simple.*;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.authorization.UserService;
import org.hswebframework.web.service.organizational.*;
import org.hswebframework.web.service.organizational.event.ClearPersonCacheEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("personService")
@CacheConfig(cacheNames = "person")
public class SimplePersonService extends GenericEntityService<PersonEntity, String>
        implements PersonService, PersonnelAuthenticationManager {


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

    @Autowired
    private DistrictDao districtDao;

    @Autowired(required = false)
    private UserService userService;

    @Autowired
    private RelationInfoDao relationInfoDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public PersonDao getDao() {
        return personDao;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = "'id:'+#result"),
            @CacheEvict(key = "'auth:persion-id'+#result"),
            @CacheEvict(key = "'auth:user-id'+#authBindEntity.userId"),
            @CacheEvict(key = "'auth-bind'+#result"),
            @CacheEvict(key = "'person-name'+#authBindEntity.name")
    })
    public String insert(PersonAuthBindEntity authBindEntity) {
        authBindEntity.setStatus(DataStatus.STATUS_ENABLED);
        if (authBindEntity.getPersonUser() != null) {
            syncUserInfo(authBindEntity);
        }
        String id = this.insert(((PersonEntity) authBindEntity));
        if (authBindEntity.getPositionIds() != null) {
            syncPositionInfo(id, authBindEntity.getPositionIds());
        }
        return id;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = "'id:'+#authBindEntity.id"),
            @CacheEvict(key = "'auth:persion-id'+#authBindEntity.id"),
            @CacheEvict(key = "'auth:user-id'+#authBindEntity.userId"),
            @CacheEvict(key = "'auth-bind'+#authBindEntity.id"),
            @CacheEvict(key = "'person-name'+#authBindEntity.name")
    })
    public int updateByPk(PersonAuthBindEntity authBindEntity) {
        if (authBindEntity.getPositionIds() != null) {
            personPositionDao.deleteByPersonId(authBindEntity.getId());
            syncPositionInfo(authBindEntity.getId(), authBindEntity.getPositionIds());
        }
        if (authBindEntity.getPersonUser() != null) {
            syncUserInfo(authBindEntity);
        }
        return this.updateByPk(((PersonEntity) authBindEntity));
    }

    @TransactionalEventListener
    @CacheEvict(allEntries = true)
    public void handleClearCache(ClearPersonCacheEvent event) {
        logger.debug("clear all person cache");
    }

    @Override
    @Cacheable(key = "'person-name'+#name")
    public List<PersonEntity> selectByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return new ArrayList<>();
        }
        return createQuery().where(PersonEntity.name, name).listNoPaging();
    }

    @Override
    @Cacheable(key = "'auth-bind'+#id")
    public PersonAuthBindEntity selectAuthBindByPk(String id) {
        PersonEntity personEntity = this.selectByPk(id);
        if (personEntity == null) {
            return null;
        }

        if (personEntity instanceof PersonAuthBindEntity) {
            return ((PersonAuthBindEntity) personEntity);
        }

        PersonAuthBindEntity bindEntity = entityFactory.newInstance(PersonAuthBindEntity.class, personEntity);
        Set<String> positionIds = DefaultDSLQueryService.createQuery(personPositionDao)
                .where(PersonPositionEntity.personId, id)
                .listNoPaging().stream()
                .map(PersonPositionEntity::getPositionId)
                .collect(Collectors.toSet());

        bindEntity.setPositionIds(positionIds);

        if (null != userService && null != personEntity.getUserId()) {
            UserEntity userEntity = userService.selectByPk(personEntity.getUserId());
            if (null != userEntity) {
                PersonUserEntity entity = entityFactory.newInstance(PersonUserEntity.class);
                entity.setUsername(userEntity.getUsername());
                bindEntity.setPersonUser(entity);
            }
        }
        return bindEntity;
    }

    @Override
    public List<PersonEntity> selectByPositionId(String positionId) {
        Objects.requireNonNull(positionId);
        return personDao.selectByPositionId(positionId);
    }

    @Override
    public List<PersonEntity> selectByPositionIds(List<String> positionId) {
        return createQuery()
                .where(PersonEntity.id, "person-in-position", positionId)
                .listNoPaging();
    }

    @Override
    public List<PersonEntity> selectByDepartmentId(List<String> departmentId) {
        return createQuery()
                .where(PersonEntity.id, "person-in-department", departmentId)
                .listNoPaging();
    }

    @Override
    public List<PersonEntity> selectByOrgId(List<String> orgId) {
        return createQuery()
                .where(PersonEntity.id, "person-in-org", orgId)
                .listNoPaging();
    }

    @Override
    public PersonEntity selectByUserId(String userId) {
        return createQuery().where(PersonEntity.userId,userId).single();
    }

    @Override
    public List<String> selectAllDepartmentId(List<String> personId) {
        if (CollectionUtils.isEmpty(personId)) {
            return new java.util.ArrayList<>();
        }
        //所有的机构
        List<String> positionId = DefaultDSLQueryService.createQuery(personPositionDao)
                .where().in(PersonPositionEntity.personId, personId)
                .listNoPaging()
                .stream()
                .map(PersonPositionEntity::getPositionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(positionId)) {
            return new java.util.ArrayList<>();
        }
        return DefaultDSLQueryService.createQuery(positionDao)
                .where()
                .in(PositionEntity.id, positionId)
                .listNoPaging()
                .stream()
                .map(PositionEntity::getDepartmentId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> selectAllOrgId(List<String> personId) {
        List<String> departmentId = this.selectAllDepartmentId(personId);
        if (CollectionUtils.isEmpty(departmentId)) {
            return new java.util.ArrayList<>();
        }
        return DefaultDSLQueryService.createQuery(departmentDao)
                .where()
                .in(DepartmentEntity.id, departmentId)
                .listNoPaging()
                .stream()
                .map(DepartmentEntity::getOrgId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<PersonEntity> selectByRoleId(String roleId) {
        Objects.requireNonNull(roleId);
        return personDao.selectByRoleId(roleId);
    }

    protected void syncPositionInfo(String personId, Set<String> positionIds) {
        for (String positionId : positionIds) {
            PersonPositionEntity positionEntity = entityFactory.newInstance(PersonPositionEntity.class);
            positionEntity.setPersonId(personId);
            positionEntity.setPositionId(positionId);
            this.personPositionDao.insert(positionEntity);
        }
    }

    protected void syncUserInfo(PersonAuthBindEntity bindEntity) {
        if (isEmpty(bindEntity.getPersonUser().getUsername())) {
            bindEntity.setUserId("");
            return;
        }

        //是否使用了权限管理的userService.
        if (null == userService) {
            logger.warn("userService not ready!");
            return;
        }
        //获取用户是否存在
        UserEntity oldUser = userService.selectByUsername(bindEntity.getPersonUser().getUsername());
        if (null != oldUser) {
            //判断用户是否已经绑定了其他人员
            int userBindSize = createQuery().where()
                    .is(PersonEntity.userId, oldUser.getId())
                    .not(PersonEntity.id, bindEntity.getId())
                    .total();
            tryValidateProperty(userBindSize == 0, "personUser.username", "用户已绑定其他人员");
        }
        // 初始化用户后的操作方式
        Function<UserEntity, String> userOperationFunction =
                oldUser == null ? userService::insert : //为空新增,不为空修改
                        user -> {
                            userService.update(oldUser.getId(), user);
                            return oldUser.getId();
                        };
        UserEntity userEntity = entityFactory.newInstance(UserEntity.class);

        userEntity.setUsername(bindEntity.getPersonUser().getUsername());
        userEntity.setPassword(bindEntity.getPersonUser().getPassword());
        userEntity.setName(bindEntity.getName());

        String userId = userOperationFunction.apply(userEntity);
        bindEntity.setUserId(userId);
    }


    @Override
    @CacheEvict(allEntries = true)
    public PersonEntity deleteByPk(String id) {
        personPositionDao.deleteByPersonId(id);
        return super.deleteByPk(id);
    }

    @Override
    @Cacheable(key = "'auth:persion-id'+#personId")
    public PersonnelAuthentication getPersonnelAuthorizationByPersonId(String personId) {
        PersonEntity entity = selectByPk(personId);
        if (null == entity) {
            return null;
        }
        SimplePersonnelAuthentication authorization = new SimplePersonnelAuthentication();

        Personnel personnel = entityFactory.newInstance(Personnel.class, SimplePersonnel.class, entity);

        authorization.setPersonnel(personnel);

        // 获取用户的职位ID集合(多个职位)
        Set<String> positionIds = DefaultDSLQueryService.createQuery(personPositionDao)
                .where(PersonPositionEntity.personId, personId)
                .listNoPaging().stream()
                .map(PersonPositionEntity::getPositionId)
                .collect(Collectors.toSet());

        Map<String, DepartmentEntity> departmentCache = new HashMap<>();
        Map<String, PositionEntity> positionCache = new HashMap<>();
        Map<String, OrganizationalEntity> orgCache = new HashMap<>();
        Map<String, DistrictEntity> districtCache = new HashMap<>();

        //获取所有职位,并得到根职位(树结构)
        List<PositionEntity> positionEntities = getAllChildrenAndReturnRootNode(positionDao, positionIds, PositionEntity::setChildren, rootPosList -> {
            //根据职位获取部门
            Set<String> departmentIds = rootPosList.stream()
                    .peek(positionEntity -> positionCache.put(positionEntity.getId(), positionEntity))
                    .map(PositionEntity::getDepartmentId)
                    .collect(Collectors.toSet());
            if (!CollectionUtils.isEmpty(departmentIds)) {
                List<DepartmentEntity> departmentEntities = getAllChildrenAndReturnRootNode(departmentDao, departmentIds, DepartmentEntity::setChildren, rootDepList -> {
                    //根据部门获取机构
                    Set<String> orgIds = rootDepList.stream()
                            .peek(departmentEntity -> departmentCache.put(departmentEntity.getId(), departmentEntity))
                            .map(DepartmentEntity::getOrgId)
                            .collect(Collectors.toSet());
                    if (!CollectionUtils.isEmpty(orgIds)) {
                        List<OrganizationalEntity> orgEntities = getAllChildrenAndReturnRootNode(organizationalDao, orgIds, OrganizationalEntity::setChildren, rootOrgList -> {
                            //根据机构获取行政区域
                            Set<String> districtIds = rootOrgList.stream()
                                    .peek(org -> orgCache.put(org.getId(), org))
                                    .map(OrganizationalEntity::getDistrictId)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toSet());
                            if (!CollectionUtils.isEmpty(districtIds)) {
                                List<DistrictEntity> districtEntities =
                                        getAllChildrenAndReturnRootNode(districtDao, districtIds, DistrictEntity::setChildren,
                                                rootDistrictList -> rootDistrictList.forEach(dist -> districtCache.put(dist.getId(), dist)));

                                authorization.setDistrictIds(transformationTreeNode(null, districtEntities));
                            }
                        });
                        authorization.setOrgIds(transformationTreeNode(null, orgEntities));
                    }
                });
                authorization.setDepartmentIds(transformationTreeNode(null, departmentEntities));
            }
        });
        authorization.setPositionIds(transformationTreeNode(null, positionEntities));

        Set<Position> positions = positionEntities.stream()
                .map(positionEntity -> {
                    DepartmentEntity departmentEntity = departmentCache.get(positionEntity.getDepartmentId());
                    if (departmentEntity == null) {
                        return null;
                    }
                    OrganizationalEntity organizationalEntity = orgCache.get(departmentEntity.getOrgId());
                    if (organizationalEntity == null) {
                        return null;
                    }
                    DistrictEntity districtEntity = districtCache.get(organizationalEntity.getDistrictId());
                    District district = districtEntity == null ? null : SimpleDistrict.builder()
                            .code(districtEntity.getCode())
                            .id(districtEntity.getId())
                            .name(districtEntity.getName())
                            .fullName(districtEntity.getFullName())
                            .build();

                    Organization organization = SimpleOrganization.builder()
                            .id(organizationalEntity.getId())
                            .name(organizationalEntity.getName())
                            .fullName(organizationalEntity.getFullName())
                            .code(organizationalEntity.getCode())
                            .district(district)
                            .build();
                    Department department = SimpleDepartment
                            .builder()
                            .id(departmentEntity.getId())
                            .name(departmentEntity.getName())
                            .code(departmentEntity.getCode())
                            .org(organization)
                            .build();

                    return SimplePosition
                            .builder()
                            .id(positionEntity.getId())
                            .name(positionEntity.getName())
                            .department(department)
                            .code("")
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        authorization.setPositions(positions);
        //获取关系
        List<RelationInfoEntity> relationInfoList = DefaultDSLQueryService.createQuery(relationInfoDao)
                .where(RelationInfoEntity.relationFrom, personId)
                .or(RelationInfoEntity.relationTo, personId)
                .listNoPaging();
        List<Relation> relations = relationInfoList.stream()
                .map(info -> {
                    SimpleRelation relation = new SimpleRelation();
                    relation.setDimension(info.getRelationTypeFrom());
                    relation.setTarget(info.getRelationTo());
                    relation.setRelation(info.getRelationId());
                    if (personId.equals(info.getRelationFrom())) {
                        relation.setDirection(Relation.Direction.POSITIVE);
                    } else {
                        relation.setDirection(Relation.Direction.REVERSE);
                    }
                    return relation;
                }).collect(Collectors.toList());
        authorization.setRelations(new SimpleRelations(relations));
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
        if (CollectionUtils.isEmpty(rootIds)) {
            return new java.util.ArrayList<>();
        }
        //获取根节点
        List<T> root = DefaultDSLQueryService.createQuery(dao)
                .where()
                .in(TreeSupportEntity.id, rootIds)
                .listNoPaging();
        //节点不存在?
        if (!root.isEmpty()) {
            //所有子节点,使用节点的path属性进行快速查询,查询结果包含了根节点
            List<T> allNode = DefaultDSLQueryService
                    .createQuery(dao)
                    //遍历生成查询条件: like path like ?||'%' or path like ?||'%'  ....
                    .each(root, (query, data) -> query.or().like$(TreeSupportEntity.path, data.getPath()))
                    .listNoPaging();
            //转为树形结构
            List<T> tree = TreeSupportEntity
                    .list2tree(allNode, childAccepter,
                            (Predicate<T>) node -> rootIds.contains(node.getId()));  // 根节点判定
            rootConsumer.accept(root);
            return tree;
        }
        return new java.util.ArrayList<>();
    }

    public static <V extends TreeSupportEntity<String>> Set<TreeNode<String>> transformationTreeNode(V parent, List<V> data) {
        Set<TreeNode<String>> treeNodes = new HashSet<>();
        data.forEach(node -> {
            TreeNode<String> treeNode = new TreeNode<>();
            if (parent != null) {
                TreeNode<String> parentNode = new TreeNode<>();
                parentNode.setValue(parent.getId());
                parentNode.setChildren(treeNodes);
//                treeNode.setParent(parentNode);
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
    @Cacheable(key = "'auth:user-id'+#userId")
    public PersonnelAuthentication getPersonnelAuthorizationByUserId(String userId) {
        PersonEntity entity = createQuery().where(PersonEntity.userId, userId).single();
        if (entity == null) {
            return null;
        }
        return getPersonnelAuthorizationByPersonId(entity.getId());
    }


}
