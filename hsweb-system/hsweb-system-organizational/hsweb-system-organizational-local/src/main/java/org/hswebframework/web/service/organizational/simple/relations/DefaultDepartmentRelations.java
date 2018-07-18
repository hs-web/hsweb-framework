package org.hswebframework.web.service.organizational.simple.relations;

import io.vavr.Lazy;
import org.hswebframework.ezorm.core.NestConditional;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.entity.organizational.DepartmentEntity;
import org.hswebframework.web.entity.organizational.PersonEntity;
import org.hswebframework.web.entity.organizational.PositionEntity;
import org.hswebframework.web.organizational.authorization.relation.DepartmentRelations;
import org.hswebframework.web.organizational.authorization.relation.PersonRelations;
import org.hswebframework.web.organizational.authorization.relation.Relation;
import org.hswebframework.web.organizational.authorization.relation.SimpleRelation;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultDepartmentRelations extends DefaultLinkedRelations<DepartmentRelations> implements DepartmentRelations {

    private boolean includeChildren;

    private boolean includeParent;

    private NestConditional<Query<DepartmentEntity, QueryParamEntity>> departmentQuery =
            Query.<DepartmentEntity, QueryParamEntity>empty(new QueryParamEntity()).noPaging().nest();

    private NestConditional<Query<PositionEntity, QueryParamEntity>> positionQuery =
            Query.<PositionEntity, QueryParamEntity>empty(new QueryParamEntity()).noPaging().nest();


    public DefaultDepartmentRelations(ServiceContext serviceContext, Supplier<List<String>> targetIdSupplier) {
        super(serviceContext, targetIdSupplier);
    }

    @Override
    public DepartmentRelations andChildren() {
        includeChildren = true;
        return this;
    }

    @Override
    public DepartmentRelations andParents() {
        includeParent = true;
        return this;
    }

    @Override
    public DepartmentRelations relations(Relation.Direction direction, String dimension, String relation) {

        if ((dimension == null || Relation.TYPE_POSITION.equals(dimension)) && direction != Relation.Direction.REVERSE) {
            //没指定维度,尝试获取岗位关系
            positionQuery.is(PositionEntity.name, relation);
        }

        return super.relations(direction, dimension, relation);
    }

    @Override
    public DepartmentRelations or() {
        positionQuery.or();
        departmentQuery.or();
        return super.or();
    }

    @Override
    public DepartmentRelations and() {
        positionQuery.and();
        departmentQuery.and();
        return super.and();
    }

    @Override
    public DepartmentRelations not(String property, Object value) {
        departmentQuery.not(property, value);

        return super.not(property, value);
    }

    @Override
    public DepartmentRelations is(String property, Object value) {
        departmentQuery.is(property, value);
        return super.is(property, value);
    }

    @Override
    public Stream<Relation> stream() {

        Map<String, List<PersonEntity>> cache = new HashMap<>();

        List<PositionEntity> positions = positionSupplier.get();

        List<String> positionIdList = getAllPerson()
                .stream()
                .map(person -> serviceContext.getPersonService().selectAuthBindByPk(person.getId()))
                .filter(bin -> !CollectionUtils.isEmpty(bin.getPositionIds()))
                .flatMap(bind -> bind.getPositionIds().stream().peek(positionId -> cache.computeIfAbsent(positionId, i -> new ArrayList<>()).add(bind)))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(positionIdList)) {
            return super.relationStream(allDepartmentId);
        }
        //将岗位里的人员转为关系信息
        Stream<Relation> relationStream = positions.stream()
                .flatMap(position -> {
                    List<PersonEntity> personEntities = cache.get(position.getId());
                    if (CollectionUtils.isEmpty(personEntities)) {
                        return Stream.empty();
                    }
                    return personEntities
                            .stream()
                            .map(person -> {
                                SimpleRelation relation = new SimpleRelation();
                                relation.setName(position.getName());
                                relation.setTarget(person.getId());
                                relation.setTargetObject(person);
                                //固定方向,因为从逻辑都是某人是某部门的某岗位
                                relation.setDirection(Relation.Direction.REVERSE);
                                //维度默认为岗位
                                relation.setDimension(Relation.TYPE_POSITION);
                                //关系标识为岗位id
                                relation.setRelation(position.getId());
                                return (Relation) relation;
                            });

                });

        return Stream.concat(relationStream, super.relationStream(allDepartmentId));
    }

    public List<String> getAllDepartmentId() {
        return allDepartmentId.get();
    }

    @SuppressWarnings("all")
    //获取所有的岗位
    private Supplier<List<PositionEntity>> positionSupplier = Lazy.val(() -> {
        List<String> departmentId = getAllDepartmentId();
        if (CollectionUtils.isEmpty(departmentId)) {
            return (Supplier) () -> new java.util.ArrayList<>();
        }
        QueryParamEntity positionQueryParam = positionQuery.end()
                .in(PositionEntity.departmentId, departmentId)
                .getParam();

        List<PositionEntity> positions = serviceContext
                .getPositionService()
                .select(positionQueryParam).stream()
                .collect(Collectors.toList());

        return (Supplier) () -> positions;
    }, Supplier.class);

    //获取所有的部门
    private Supplier<List<String>> allDepartmentId = createLazyIdSupplier(() -> {
        Set<String> departmentId = new HashSet<>(targetIdSupplier.get());
        if (CollectionUtils.isEmpty(departmentId)) {
            return new java.util.ArrayList<>();
        }
        Set<String> allParent = null, allChildren = null;
        //包含父级
        if (includeParent) {
            allParent = departmentId.stream()
                    .map(serviceContext.getDepartmentService()::selectParentNode)
                    .flatMap(Collection::stream)
                    .map(DepartmentEntity::getId)
                    .collect(Collectors.toSet());
        }
        //包含子级
        if (includeChildren) {
            allChildren = departmentId.stream()
                    .map(serviceContext.getDepartmentService()::selectAllChildNode)
                    .flatMap(Collection::stream)
                    .map(DepartmentEntity::getId)
                    .collect(Collectors.toSet());
        }
        if (!CollectionUtils.isEmpty(allChildren)) {
            departmentId.addAll(allChildren);
        }
        if (!CollectionUtils.isEmpty(allParent)) {
            departmentId.addAll(allParent);
        }

        QueryParamEntity paramEntity = departmentQuery.end().getParam();
        if (paramEntity.getTerms().isEmpty()) {
            return new ArrayList<>(departmentId);
        }
        paramEntity = departmentQuery.end()
                .select(DepartmentEntity.id)
                .in(DepartmentEntity.id, departmentId).getParam();

        return serviceContext.getDepartmentService()
                .select(paramEntity)
                .stream()
                .map(DepartmentEntity::getId)
                .collect(Collectors.toList());
    });


    public List<PersonEntity> getAllPerson() {

        List<PositionEntity> positionEntities = positionSupplier.get();

        if (CollectionUtils.isEmpty(positionEntities)) {
            return new java.util.ArrayList<>();
        }

        return serviceContext
                .getPersonService()
                .selectByPositionIds(
                        positionEntities.stream()
                                .map(PositionEntity::getId)
                                .collect(Collectors.toList()));
    }

    public List<String> getAllUserId() {
        return getAllPerson().stream()
                .map(PersonEntity::getUserId)
                .collect(Collectors.toList());
    }

    public List<String> getAllPersonId() {
        return getAllPerson()
                .stream()
                .map(PersonEntity::getId)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentRelations deep() {
        return new DefaultDepartmentRelations(serviceContext, allDepartmentId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public PersonRelations persons() {
        return new DefaultPersonRelations(serviceContext, createLazyIdSupplier(this::getAllPersonId));
    }
}
