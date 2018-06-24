package org.hswebframework.web.service.organizational.simple.relations;

import org.hswebframework.ezorm.core.NestConditional;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.ezorm.core.param.TermType;
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

        if (dimension == null && direction != Relation.Direction.REVERSE) {
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
        positionQuery.not(property, value);

        return super.not(property, value);
    }

    @Override
    public DepartmentRelations is(String property, Object value) {
        departmentQuery.is(property, value);
        positionQuery.is(property, value);
        return super.is(property, value);
    }

    @Override
    public Stream<Relation> stream() {

        Map<String, List<PersonEntity>> cache = new HashMap<>();


        List<String> positionIdList = getAllPerson()
                .stream()
                .map(person -> serviceContext.getPersonService().selectAuthBindByPk(person.getId()))
                .filter(bin -> !CollectionUtils.isEmpty(bin.getPositionIds()))
                .flatMap(bind -> bind.getPositionIds().stream().peek(positionId -> cache.computeIfAbsent(positionId, i -> new ArrayList<>()).add(bind)))
                .collect(Collectors.toList());

        QueryParamEntity positionQueryParam = positionQuery.end().in(PositionEntity.id, positionIdList)
                .getParam();

        Stream<Relation> relationStream = serviceContext
                .getPositionService()
                .select(positionQueryParam).stream()
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
                                relation.setDirection(Relation.Direction.REVERSE);
                                relation.setDimension(Relation.TYPE_POSITION);
                                relation.setRelation(position.getId());
                                return (Relation) relation;
                            });

                });


        return Stream.concat(relationStream, super.relationStream(allDepartmentId));
    }

    private Supplier<List<String>> allDepartmentId = createLazyIdSupplier(() -> {
        Set<String> departmentId = new HashSet<>(targetIdSupplier.get());

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
        paramEntity.noPaging()
                .includes(DepartmentEntity.id)
                .and(DepartmentEntity.id, TermType.in, departmentId);

        return serviceContext.getDepartmentService()
                .select(paramEntity)
                .stream()
                .map(DepartmentEntity::getId)
                .collect(Collectors.toList());
    });


    public List<PersonEntity> getAllPerson() {
        List<String> departmentId = allDepartmentId.get();

        QueryParamEntity positionQueryParam = positionQuery.end()
                .in(PositionEntity.departmentId, departmentId)
                .getParam();

        List<String> positionIds = serviceContext
                .getPositionService()
                .select(positionQueryParam).stream()
                .map(PositionEntity::getId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(positionIds)) {

            return Collections.emptyList();
        }

        return serviceContext
                .getPersonService()
                .selectByPositionIds(positionIds);


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
    @SuppressWarnings("unchecked")
    public PersonRelations persons() {
        return new DefaultPersonRelations(serviceContext, createLazyIdSupplier(this::getAllPersonId));
    }
}
