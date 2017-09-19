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

import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.commons.entity.TreeSupportEntity;
import org.hswebframework.web.dao.dynamic.QueryByEntityDao;
import org.hswebframework.web.dao.organizational.*;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.entity.authorization.bind.BindRoleUserEntity;
import org.hswebframework.web.entity.organizational.*;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.organizational.authorization.Personnel;
import org.hswebframework.web.organizational.authorization.PersonnelAuthorization;
import org.hswebframework.web.organizational.authorization.PersonnelAuthorizationManager;
import org.hswebframework.web.organizational.authorization.TreeNode;
import org.hswebframework.web.organizational.authorization.relation.Relation;
import org.hswebframework.web.organizational.authorization.relation.SimpleRelation;
import org.hswebframework.web.organizational.authorization.relation.SimpleRelations;
import org.hswebframework.web.organizational.authorization.simple.SimplePersonnel;
import org.hswebframework.web.organizational.authorization.simple.SimplePersonnelAuthorization;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.authorization.AuthorizationSettingTypeSupplier;
import org.hswebframework.web.service.authorization.UserService;
import org.hswebframework.web.service.organizational.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
        implements PersonService, PersonnelAuthorizationManager, AuthorizationSettingTypeSupplier {

    private static String SETTING_TYPE_PERSON   = "person";
    private static String SETTING_TYPE_POSITION = "position";

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
            @CacheEvict(key = "'auth-bind'+#result")
    })
    public String insert(PersonAuthBindEntity authBindEntity) {
        authBindEntity.setStatus(DataStatus.STATUS_ENABLED);
        // TODO: 17-6-1 应该使用锁,防止并发同步用户,导致多个人员使用相同的用户
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
            @CacheEvict(key = "'auth-bind'+#authBindEntity.id")
    })
    public int updateByPk(PersonAuthBindEntity authBindEntity) {
        // TODO: 17-6-1 应该使用锁,防止并发同步用户,导致多个人员使用相同的用户
        if (authBindEntity.getPositionIds() != null) {
            personPositionDao.deleteByPersonId(authBindEntity.getId());
            syncPositionInfo(authBindEntity.getId(), authBindEntity.getPositionIds());
        }
        if (authBindEntity.getPersonUser() != null) {
            syncUserInfo(authBindEntity);
        }
        return this.updateByPk(((PersonEntity) authBindEntity));
    }

    @Override
    @Cacheable(key = "'auth-bind'+#id")
    public PersonAuthBindEntity selectAuthBindByPk(String id) {
        PersonEntity personEntity = this.selectByPk(id);
        if (personEntity == null) return null;

        if (personEntity instanceof PersonAuthBindEntity) return ((PersonAuthBindEntity) personEntity);

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
        //获取所有职位
        Set<String> positionIds = bindEntity.getPositionIds();
        if (positionIds.isEmpty()) return;
        //是否使用了权限管理的userService.
        if (null == userService) {
            logger.warn("userService not ready!");
            return;
        }
        //获取职位实体
        List<PositionEntity> positionEntities = DefaultDSLQueryService.createQuery(positionDao)
                .where().in(PositionEntity.id, positionIds)
                .listNoPaging();
        if (positionEntities.isEmpty()) return;
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
        //初始化用户信息
        //全部角色信息
        Set<String> roleIds = positionEntities.stream()
                .map(PositionEntity::getRoles)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        BindRoleUserEntity userEntity = entityFactory.newInstance(BindRoleUserEntity.class);
        userEntity.setUsername(bindEntity.getPersonUser().getUsername());
        userEntity.setPassword(bindEntity.getPersonUser().getPassword());
        userEntity.setName(bindEntity.getName());
        userEntity.setRoles(new ArrayList<>(roleIds));
        String userId = userOperationFunction.apply(userEntity);
        bindEntity.setUserId(userId);
    }


    @Override
    public int deleteByPk(String id) {
        personPositionDao.deleteByPersonId(id);
        return super.deleteByPk(id);
    }

    @Override
    @Cacheable(key = "'auth:persion-id'+#personId")
    public PersonnelAuthorization getPersonnelAuthorizationByPersonId(String personId) {
        SimplePersonnelAuthorization authorization = new SimplePersonnelAuthorization();
        PersonEntity entity = selectByPk(personId);
        assertNotNull(entity);

        Personnel personnel = entityFactory.newInstance(Personnel.class, SimplePersonnel.class, entity);

        authorization.setPersonnel(personnel);

        // 获取用户的职位ID集合(多个职位)
        Set<String> positionIds = DefaultDSLQueryService.createQuery(personPositionDao)
                .where(PersonPositionEntity.personId, personId)
                .listNoPaging().stream()
                .map(PersonPositionEntity::getPositionId)
                .collect(Collectors.toSet());
        //获取所有职位,并得到根职位(树结构)
        List<PositionEntity> positionEntities = getAllChildrenAndReturnRootNode(positionDao, positionIds, PositionEntity::setChildren, rootPosList -> {
            //根据职位获取部门
            Set<String> departmentIds = rootPosList.stream().map(PositionEntity::getDepartmentId).collect(Collectors.toSet());
            if (!CollectionUtils.isEmpty(departmentIds)) {
                List<DepartmentEntity> departmentEntities = getAllChildrenAndReturnRootNode(departmentDao, departmentIds, DepartmentEntity::setChildren, rootDepList -> {
                    //根据部门获取机构
                    Set<String> orgIds = rootDepList.stream().map(DepartmentEntity::getOrgId).collect(Collectors.toSet());
                    if (!CollectionUtils.isEmpty(orgIds)) {
                        List<OrganizationalEntity> orgEntities = getAllChildrenAndReturnRootNode(organizationalDao, orgIds, OrganizationalEntity::setChildren, rootOrgList -> {
                            //根据机构获取行政区域
                            Set<String> districtIds = rootOrgList.stream().map(OrganizationalEntity::getDistrictId).filter(Objects::nonNull).collect(Collectors.toSet());
                            if (!CollectionUtils.isEmpty(districtIds)) {
                                List<DistrictEntity> districtEntities =
                                        getAllChildrenAndReturnRootNode(districtDao, districtIds, DistrictEntity::setChildren, rootDistrictList -> {

                                        });
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

        //获取关系
        List<RelationInfoEntity> relationInfoList = DefaultDSLQueryService.createQuery(relationInfoDao)
                .where(RelationInfoEntity.relationFrom, personId)
                .or(RelationInfoEntity.relationTo, personId)
                .listNoPaging();
        List<Relation> relations = relationInfoList.stream()
                .map(info -> {
                    SimpleRelation relation = new SimpleRelation();
                    relation.setType(info.getRelationTypeFrom());
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
        //获取根节点
        List<T> root = DefaultDSLQueryService.createQuery(dao)
                .where().in(TreeSupportEntity.id, rootIds)
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
    @Cacheable(cacheNames = "person", key = "'auth:user-id'+#userId")
    public PersonnelAuthorization getPersonnelAuthorizationByUserId(String userId) {
        PersonEntity entity = createQuery().where(PersonEntity.userId, userId).single();
        if (entity == null) {
            return null;
        }
        return getPersonnelAuthorizationByPersonId(entity.getId());
    }

    @Override
    public Set<SettingInfo> get(String userId) {
        //支持职位和人员 设置权限
        PersonEntity entity = createQuery().where(PersonEntity.userId, userId).single();
        if (entity == null) return new HashSet<>();
        Set<SettingInfo> settingInfo = new HashSet<>();
        //岗位设置
        //TODO 2017/06/08 是否将子级岗位的设置也放进来??
        DefaultDSLQueryService.createQuery(personPositionDao)
                .where(PersonPositionEntity.personId, entity.getId())
                .listNoPaging()
                .stream()
                .map(position -> new SettingInfo(SETTING_TYPE_POSITION, position.getPositionId()))
                .forEach(settingInfo::add);
        //其他设置支持?

        //人员配置
        settingInfo.add(new SettingInfo(SETTING_TYPE_PERSON, entity.getId()));
        return settingInfo;
    }
}
