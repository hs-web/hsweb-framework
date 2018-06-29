package org.hswebframework.web.service.organizational.simple.relations;

import io.vavr.Lazy;
import org.hswebframework.ezorm.core.NestConditional;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.entity.organizational.PersonEntity;
import org.hswebframework.web.organizational.authorization.relation.*;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("all")
public class DefaultLinkedRelations<C extends LinkedRelations> implements LinkedRelations<C> {

    protected Supplier<List<String>> targetIdSupplier;

    protected transient NestConditional<Query<PersonEntity, QueryParamEntity>> query;

    protected transient ServiceContext serviceContext;

    public DefaultLinkedRelations(ServiceContext serviceContext, Supplier<List<String>> targetIdSupplier) {
        this.serviceContext = serviceContext;
        this.targetIdSupplier = targetIdSupplier;
        query = Query.<PersonEntity, QueryParamEntity>empty(new QueryParamEntity()).nest();
    }

    @Override
    public C relations(Relation.Direction direction, String dimension, String relation) {
        switch (direction) {
            case REVERSE:
                query.nest().is("relationTypeTo", dimension).is("relationId", relation).end();
                break;
            case POSITIVE:
                query.nest().is("relationTypeFrom", dimension).is("relationId", relation).end();
                break;
            case ALL:
                query
                        .nest()
                        .nest()
                        .is("relationTypeFrom", dimension).is("relationId", relation)
                        .end()
                        .or()
                        .nest()
                        .is("relationTypeTo", dimension).is("relationId", relation)
                        .end()
                        .end();
                break;

        }

        return (C) this;
    }

    @Override
    public C is(String property, Object value) {
        query.is(property, value);
        return (C) this;
    }

    @Override
    public C not(String property, Object value) {
        query.not(property, value);
        return (C) this;
    }

    @Override
    public C or() {
        query.or();
        return (C) this;
    }

    @Override
    public C and() {
        query.and();
        return (C) this;
    }

    public Supplier<List<String>> createLazyIdSupplier(Supplier<List<String>> idSupplier) {
        return Lazy.val(() -> {

            List<String> ids = idSupplier.get();

            return (Supplier) () -> ids;
        }, Supplier.class);
    }

    public Stream<Relation> relationStream(Supplier<List<String>> supplier) {

        List<String> personIdList = supplier.get();

        QueryParamEntity queryParamEntity = query.end()
                .and()
                .nest()
                .in("relationFrom", personIdList)
                .or()
                .in("relationTo", personIdList)
                .end()
                .getParam();


        return serviceContext.getRelationInfoService().select(queryParamEntity).stream()
                .map(info -> {
                    SimpleRelation relation = new SimpleRelation();

                    relation.setTarget(info.getRelationTo());
                    relation.setTargetObject(RelationTargetHolder.get(info.getRelationTypeTo(), info.getRelationTo()).orElse(null));
                    relation.setRelation(info.getRelationId());

                    if (personIdList.contains(info.getRelationFrom())) {
                        relation.setDimension(info.getRelationTypeFrom());
                        relation.setDirection(Relation.Direction.POSITIVE);
                    } else {
                        relation.setDimension(info.getRelationTypeTo());
                        relation.setDirection(Relation.Direction.REVERSE);
                    }
                    return relation;
                });
    }

    @Override
    public C deep() {
        if (this.getClass() != DefaultLinkedRelations.class) {
            throw new UnsupportedOperationException("子类未实现deep方法");
        }
        return (C) new DefaultLinkedRelations<C>(serviceContext, () -> all()
                .stream()
                .map(Relation::getTarget)
                .collect(Collectors.toList()));
    }

    @Override
    public Stream<Relation> stream() {
        return relationStream(targetIdSupplier);
    }
}
