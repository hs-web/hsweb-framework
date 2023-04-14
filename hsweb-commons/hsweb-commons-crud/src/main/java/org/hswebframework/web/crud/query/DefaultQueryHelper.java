package org.hswebframework.web.crud.query;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.hswebframework.ezorm.core.*;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.executor.wrapper.ColumnWrapperContext;
import org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrapper;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.TableOrViewMetadata;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.NativeSql;
import org.hswebframework.ezorm.rdb.operator.dml.Join;
import org.hswebframework.ezorm.rdb.operator.dml.JoinType;
import org.hswebframework.ezorm.rdb.operator.dml.QueryOperator;
import org.hswebframework.ezorm.rdb.operator.dml.SelectColumnSupplier;
import org.hswebframework.ezorm.rdb.operator.dml.query.Selects;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.persistence.Table;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers.column;

@AllArgsConstructor
public class DefaultQueryHelper implements QueryHelper {

    private final DatabaseOperator database;

    private final Map<Class<?>, Table> nameMapping = new ConcurrentHashMap<>();

    @Override
    public <R> SelectColumnMapperSpec<R> select(Class<R> resultType) {
        return new QuerySpec<>(resultType, this);
    }

    @Override
    public <R> SelectSpec<R> select(Class<R> resultType, Consumer<ColumnMapperSpec<R,?>> mapperSpec) {
        QuerySpec<R> querySpec = new QuerySpec<>(resultType, this);

        mapperSpec.accept(querySpec);

        return querySpec;
    }

    TableOrViewMetadata getTable(Class<?> type) {
        Table table = nameMapping.computeIfAbsent(type, this::parseTableName);
        if (StringUtils.hasText(table.schema())) {
            return database
                    .getMetadata()
                    .getSchema(table.schema())
                    .flatMap(schema -> schema.getTableOrView(table.name(), false))
                    .orElseThrow(() -> new UnsupportedOperationException("table [" + table.schema() + "." + table.name() + "] not found"));
        }
        return database
                .getMetadata()
                .getCurrentSchema()
                .getTableOrView(table.name(), false)
                .orElseThrow(() -> new UnsupportedOperationException("table [" + table.name() + "] not found"));
    }

    static RDBColumnMetadata getColumn(TableOrViewMetadata table, String column) {
        return table
                .getColumn(column)
                .orElseThrow(() -> new UnsupportedOperationException("column [" + column + "] not found in [" + table.getName() + "]"));
    }

    Table parseTableName(Class<?> type) {
        Table table = AnnotatedElementUtils.findMergedAnnotation(type, Table.class);
        if (null == table) {
            throw new UnsupportedOperationException("type [" + type.getName() + "] not found @Table annotation");
        }
        return table;
    }

    @SafeVarargs
    private static <T> T[] toArray(T... arr) {
        return arr;
    }


    static abstract class ColumnMapping<R> {
        final QuerySpec<R> parent;

        public ColumnMapping(QuerySpec<R> parent) {
            this.parent = parent;
        }

        abstract SelectColumnSupplier[] forSelect();

        abstract boolean match(String[] column);

        abstract void applyValue(R result, String[] column, Object sqlValue);

        static class All<R, V> extends ColumnMapping<R> {
            private final String table;
            private final Class<?> tableType;
            private TableOrViewMetadata target;

            private final String alias;

            private final String targetProperty;

            public All(QuerySpec<R> parent,
                       String table,
                       Class<?> tableType,
                       Setter<R, V> setter) {
                super(parent);
                this.table = table;
                this.tableType = tableType;
                this.targetProperty = setter == null ? null : MethodReferenceConverter.convertToColumn(setter);
                String prefix = targetProperty == null ? "all" : targetProperty;
                int size = parent.mappings.size();
                this.alias = size == 0 ? prefix : prefix + "_" + size;
            }

            @Override
            boolean match(String[] column) {
                return column.length >= 2 && Objects.equals(alias, column[0]);
            }

            @Override
            void applyValue(R result, String[] column, Object sqlValue) {

                if (column.length > 1) {
                    target.getColumn(column[1])
                          .ifPresent(metadata -> {
                              if (targetProperty == null) {
                                  GlobalConfig
                                          .getPropertyOperator()
                                          .setProperty(result,
                                                       metadata.getName(),
                                                       metadata.decode(sqlValue));
                              } else {
                                  ObjectPropertyOperator operator = GlobalConfig.getPropertyOperator();

                                  Object val = operator.getPropertyOrNew(result, targetProperty);

                                  operator.setProperty(val,
                                                       column[1],
                                                       metadata.decode(sqlValue));
                              }

                          });
                }
            }

            SelectColumnSupplier[] toColumns(TableOrViewMetadata table,
                                             String owner) {

                return table
                        .getColumns()
                        .stream()
                        .map(column -> Selects
                                .column(owner == null ? column.getName() : owner + "." + column.getName())
                                .as(alias + "." + column.getAlias()))
                        .toArray(SelectColumnSupplier[]::new);
            }

            @Override
            SelectColumnSupplier[] forSelect() {
                //查询主表
                if (tableType == parent.from) {
                    return toColumns(this.target = parent.table, null);
                }

                //join表
                JoinConditionalSpecImpl join;
                if (this.table != null) {
                    join = parent.getJoinByAlias(this.table);
                } else {
                    join = parent.getJoinByClass(tableType);
                }
                this.target = join.main;

                return toColumns(this.target, join.alias);
            }
        }

        static class Default<R, S, V> extends ColumnMapping<R> {
            private final String column;
            private String alias;
            private final Getter<S, V> getter;
            private final Setter<R, V> setter;
            RDBColumnMetadata metadata;

            public Default(QuerySpec<R> parent,
                           String column,
                           Getter<S, V> getter,
                           String alias,
                           Setter<R, V> setter) {
                super(parent);
                this.column = column;
                this.alias = alias;
                this.getter = getter;
                this.setter = setter;
            }

            @Override
            boolean match(String[] column) {
                return column.length == 1 && Objects.equals(alias, column[0]);
            }

            @Override
            void applyValue(R result, String[] column, Object sqlValue) {
                if (setter != null) {
                    setter.accept(result, (V) metadata.decode(sqlValue));
                    return;
                }
                GlobalConfig.getPropertyOperator().setProperty(result, column[0], metadata.decode(sqlValue));
            }

            @Override
            SelectColumnSupplier[] forSelect() {
                this.alias = this.alias != null ?
                        this.alias : MethodReferenceConverter.convertToColumn(setter);

                if (column != null) {
                    String[] nestMaybe = column.split("[.]");
                    if (nestMaybe.length == 2) {
                        JoinConditionalSpecImpl join = parent.getJoinByAlias(nestMaybe[0]);

                        metadata = getColumn(join.main, nestMaybe[1]);
                    } else {
                        metadata = getColumn(parent.table, column);
                    }
                    return toArray(Selects.column(column).as(alias));

                } else if (getter != null) {

                    MethodReferenceInfo info = MethodReferenceConverter.parse(getter);
                    //查主表
                    if (info.getOwner() == parent.from) {
                        metadata = getColumn(parent.table, info.getColumn());
                        return toArray(Selects.column(info.getColumn()).as(alias));
                    } else {
                        JoinConditionalSpecImpl join = parent.getJoinByClass(info.getOwner());
                        metadata = getColumn(join.main, info.getColumn());
                        return toArray(Selects.column(join.alias + "." + info.getColumn()).as(alias));
                    }

                }
                throw new IllegalArgumentException("column or getter can not be null");
            }
        }
    }

    static class QuerySpec<R> implements SelectSpec<R>, FromSpec<R>, SortSpec<R>, ResultWrapper<R, R>, SelectColumnMapperSpec<R> {

        private final Class<R> clazz;

        private final DefaultQueryHelper parent;

        private final List<ColumnMapping<R>> mappings = new ArrayList<>();

        private TableOrViewMetadata table;

        private Class<?> from;

        private int joinIndex;
        private QueryOperator query;

        private List<JoinConditionalSpecImpl> joins;

        private QueryParamEntity param;


        public QuerySpec(Class<R> clazz, DefaultQueryHelper parent) {
            this.clazz = clazz;
            this.parent = parent;
        }

        private List<JoinConditionalSpecImpl> joins() {
            return joins == null ? joins = new ArrayList<>(3) : joins;
        }

        private JoinConditionalSpecImpl getJoinByClass(Class<?> clazz) {

            if (joins != null) {
                for (JoinConditionalSpecImpl join : joins) {
                    if (Objects.equals(join.mainClass, clazz)) {
                        return join;
                    }
                }
            }

            throw new IllegalArgumentException("join class [" + clazz + "] not found!");
        }

        private JoinConditionalSpecImpl getJoinByAlias(String alias) {
            if (joins != null) {
                for (JoinConditionalSpecImpl join : joins) {
                    if (Objects.equals(join.alias, alias)) {
                        return join;
                    }
                }
            }

            throw new IllegalArgumentException("join alias [" + alias + "] not found!");
        }

        @Override
        public <From> FromSpec<R> from(Class<From> clazz) {
            query = parent
                    .database
                    .dml()
                    .query(table = parent.getTable(from = clazz));
            return this;
        }

        private QueryOperator createQuery() {
            QueryOperator query = this.query.clone();
            for (ColumnMapping<R> mapping : mappings) {
                query.select(mapping.forSelect());
            }
            return query;

        }

        @Override
        public Flux<R> fetch() {

            return createQuery()
                    .fetch(this)
                    .reactive();
        }

        @Override
        public Mono<PagerResult<R>> fetchPaged() {
            if (param != null) {
                return fetchPaged(param.getPageIndex(), param.getPageSize());
            }
            return fetchPaged(0, 25);
        }

        @Override
        public Mono<PagerResult<R>> fetchPaged(int pageIndex, int pageSize) {

            Mono<Integer> count = param != null && param.getTotal() != null
                    ? Mono.just(param.getTotal())
                    : query.clone()
                           .select(Selects.count1().as("_total"))
                           .fetch(column("_total", Number.class::cast))
                           .reactive()
                           .map(Number::intValue)
                           .single(0);

            Mono<List<R>> results = createQuery()
                    .paging(pageIndex, pageSize)
                    .fetch(this)
                    .reactive()
                    .collectList();

            return Mono.zip(
                    count,
                    results,
                    (total, data) -> param != null ?
                            PagerResult.of(total, data, param) :
                            PagerResult.of(total, data)
            );
        }

        @Override
        public SortSpec<R> sort(String column, String order) {

            return this;
        }

        @Override
        public <T> SortSpec<R> sort(Getter<T, ?> column, String order) {
            return sort(MethodReferenceConverter.parse(column).getColumn(), order);
        }

        @Override
        public SortSpec<R> where(QueryParamEntity param) {
            query.setParam(this.param = param);
            return this;
        }

        @Override
        public SortSpec<R> where(Consumer<Conditional<?>> dsl) {

            query.where(c -> dsl.accept(new ConditionalImpl(this, c)));

            return this;
        }

        private String createJoinAlias() {
            return "j_" + (joinIndex++);
        }

        public <T> JoinSpec<R> join(Class<T> type,
                                    String alias,
                                    JoinType joinType,
                                    Consumer<JoinConditionalSpec<?>> on) {
            TableOrViewMetadata joinTable = parent.getTable(type);

            Query<?, QueryParamEntity> condition = QueryParamEntity.newQuery();

            JoinConditionalSpecImpl spec = new JoinConditionalSpecImpl(
                    this,
                    type,
                    joinTable,
                    alias,
                    condition
            );

            joins().add(spec);

            on.accept(spec);

            QueryParamEntity param = condition.getParam();

            Join join = new Join();
            join.setAlias(spec.alias);
            join.setTerms(param.getTerms());
            join.setType(joinType);
            join.setTarget(spec.main.getFullName());

            query.join(join);
            return this;

        }

        @Override
        public <T> JoinSpec<R> fullJoin(Class<T> type, Consumer<JoinConditionalSpec<?>> on) {
            return join(type, createJoinAlias(), JoinType.full, on);
        }

        @Override
        public <T> JoinSpec<R> leftJoin(Class<T> type, Consumer<JoinConditionalSpec<?>> on) {
            return join(type, createJoinAlias(), JoinType.left, on);
        }

        @Override
        public <T> JoinSpec<R> innerJoin(Class<T> type, Consumer<JoinConditionalSpec<?>> on) {
            return join(type, createJoinAlias(), JoinType.inner, on);
        }

        @Override
        public <T> JoinSpec<R> rightJoin(Class<T> type, Consumer<JoinConditionalSpec<?>> on) {
            return join(type, createJoinAlias(), JoinType.right, on);
        }

        @Override
        @SneakyThrows
        public R newRowInstance() {
            return clazz.newInstance();
        }

        @Override
        public void wrapColumn(ColumnWrapperContext<R> context) {
            if (context.getResult() == null) {
                return;
            }
            String[] column = context.getColumnLabel().split("[.]");
            ColumnMapping<R> mapping = getMappingByColumn(column);
            if (null == mapping) {
                return;
            }

            mapping.applyValue(context.getRowInstance(), column, context.getResult());

        }

        @Override
        public boolean completedWrapRow(R result) {
            return true;
        }

        @Override
        public R getResult() {
            throw new UnsupportedOperationException();
        }

        public ColumnMapping<R> getMappingByColumn(String[] column) {
            for (ColumnMapping<R> mapping : mappings) {
                if (mapping.match(column)) {
                    return mapping;
                }
            }
            return null;
        }


        @Override
        public SelectColumnMapperSpec<R> all(Class<?> joinType) {
            mappings.add(new ColumnMapping.All<>(this, null, joinType, null));
            return this;
        }

        @Override
        public <V> SelectColumnMapperSpec<R> all(Class<?> joinType, Setter<R, V> setter) {
            mappings.add(new ColumnMapping.All<>(this, null, joinType, setter));
            return this;
        }

        @Override
        public SelectColumnMapperSpec<R> all(String table) {
            mappings.add(new ColumnMapping.All<>(this, table, null, null));
            return this;
        }

        @Override
        public <V> SelectColumnMapperSpec<R> all(String table, Setter<R, V> setter) {
            mappings.add(new ColumnMapping.All<>(this, table, null, setter));
            return this;
        }

        @Override
        public <S, V> SelectColumnMapperSpec<R> as(Getter<S, V> getter, Setter<R, V> setter) {

            mappings.add(new ColumnMapping.Default<>(this, null, getter, null, setter));
            return this;
        }

        @Override
        public <S, V> SelectColumnMapperSpec<R> as(Getter<S, V> getter, String alias) {
            mappings.add(new ColumnMapping.Default<>(this, null, getter, alias, null));
            return this;
        }

        @Override
        public <S, V> SelectColumnMapperSpec<R> as(String column, Setter<R, V> setter) {
            mappings.add(new ColumnMapping.Default<>(this, column, null, null, setter));
            return this;
        }

        @Override
        public <S, V> SelectColumnMapperSpec<R> as(String column, String alias) {
            mappings.add(new ColumnMapping.Default<>(this, column, null, alias, null));
            return this;
        }

    }

    @AllArgsConstructor
    static class JoinConditionalSpecImpl implements JoinConditionalSpec<JoinConditionalSpecImpl> {
        private final QuerySpec<?> parent;
        private final Class<?> mainClass;
        private final TableOrViewMetadata main;
        private String alias;
        private final Conditional<?> target;

        @Override
        public <T> JoinConditionalSpecImpl applyColumn(String column,
                                                       String termType,
                                                       StaticMethodReferenceColumn<T> joinColumn) {
            MethodReferenceInfo ref = MethodReferenceConverter.parse(joinColumn);

            String joinColumnName = ref.getColumn();
            Class<?> owner = ref.getOwner();
            if (owner == parent.from) {
                return applyColumn(column, termType, parent.table, parent.table.getName(), joinColumnName);
            }
            JoinConditionalSpecImpl spec = parent.getJoinByClass(owner);

            return applyColumn(column, termType, spec.main, spec.alias, joinColumnName);
        }

        public <T> JoinConditionalSpecImpl applyColumn(String mainColumn,
                                                       String termType,
                                                       TableOrViewMetadata join,
                                                       String alias,
                                                       String column) {

            RDBColumnMetadata columnMetadata = join
                    .getColumn(column)
                    .orElseThrow(() -> new IllegalArgumentException("column [" + column + "] not found"));

            Term term = new Term();
            term.setTermType(termType);
            term.setColumn(mainColumn);
            term.setValue(NativeSql.of(columnMetadata.getFullName(alias)));

            target.accept(term);

            return this;
        }

        @Override
        public <T> JoinConditionalSpecImpl applyColumn(String mainColumn,
                                                       String termType,
                                                       String alias,
                                                       String column) {
            JoinConditionalSpecImpl spec = parent.getJoinByAlias(alias);

            return applyColumn(column, termType, spec.main, spec.alias, column);
        }

        @Override
        public JoinNestConditionalSpec<JoinConditionalSpecImpl> nest() {
            Term term = new Term();
            term.setType(Term.Type.and);
            target.accept(term);

            return new JoinNestConditionalSpecImpl<>(parent, this, term);
        }

        @Override
        public JoinNestConditionalSpec<JoinConditionalSpecImpl> orNest() {
            Term term = new Term();
            term.setType(Term.Type.or);
            target.accept(term);

            return new JoinNestConditionalSpecImpl<>(parent, this, term);
        }

        @Override
        public JoinConditionalSpecImpl and() {
            target.and();
            return this;
        }

        @Override
        public JoinConditionalSpecImpl or() {
            target.or();
            return this;
        }

        @Override
        public JoinConditionalSpecImpl and(String column, String termType, Object value) {
            target.and(column, termType, value);
            return this;
        }

        @Override
        public JoinConditionalSpecImpl or(String column, String termType, Object value) {
            target.or(column, termType, value);
            return this;
        }

        @Override
        public Accepter<JoinConditionalSpecImpl, Object> getAccepter() {
            return ((column, termType, value) -> {
                target.getAccepter().accept(column, termType, value);
                return this;
            });
        }

        @Override
        public JoinConditionalSpecImpl accept(Term term) {
            target.accept(term);
            return this;
        }

        @Override
        public JoinConditionalSpecImpl alias(String alias) {
            this.alias = alias;
            return this;
        }

    }

    static class JoinNestConditionalSpecImpl<T extends TermTypeConditionalSupport> extends SimpleNestConditional<T> implements JoinNestConditionalSpec<T> {
        final QuerySpec<?> parent;

        private final Term term;

        public JoinNestConditionalSpecImpl(QuerySpec<?> parent, T target, Term term) {
            super(target, term);
            this.parent = parent;
            this.term = term;
        }

        @Override
        public JoinNestConditionalSpec<NestConditional<T>> nest() {
            return new JoinNestConditionalSpecImpl<>(parent, this, term.nest());
        }

        @Override
        public JoinNestConditionalSpec<NestConditional<T>> orNest() {
            return new JoinNestConditionalSpecImpl<>(parent, this, term.orNest());
        }

        @Override
        public <T1> T applyColumn(String mainColumn, String termType, StaticMethodReferenceColumn<T1> joinColumn) {
            MethodReferenceInfo ref = MethodReferenceConverter.parse(joinColumn);

            String joinColumnName = ref.getColumn();
            Class<?> owner = ref.getOwner();
            if (owner == parent.from) {
                return applyColumn(mainColumn, termType, parent.table, parent.table.getName(), joinColumnName);
            }
            JoinConditionalSpecImpl spec = parent.getJoinByClass(owner);

            return applyColumn(mainColumn, termType, spec.main, spec.alias, joinColumnName);
        }


        public T applyColumn(String mainColumn,
                             String termType,
                             TableOrViewMetadata join,
                             String alias,
                             String column) {

            RDBColumnMetadata columnMetadata = join
                    .getColumn(column)
                    .orElseThrow(() -> new IllegalArgumentException("column [" + column + "] not found"));

            getAccepter().accept(mainColumn,termType,NativeSql.of(columnMetadata.getFullName(alias)));

            return (T)this;
        }

        @Override
        public T applyColumn(String mainColumn,
                             String termType,
                             String alias,
                             String column) {
            JoinConditionalSpecImpl spec = parent.getJoinByAlias(alias);

            return applyColumn(column, termType, spec.main, spec.alias, column);
        }
    }

    static class NestConditionalImpl<T extends TermTypeConditionalSupport> extends SimpleNestConditional<T> {
        final QuerySpec<?> parent;

        public NestConditionalImpl(QuerySpec<?> parent, T target, Term term) {
            super(target, term);
            this.parent = parent;
        }

        @Override
        public <B> NestConditional<T> accept(MethodReferenceColumn<B> column, String termType) {
            MethodReferenceInfo info = MethodReferenceConverter.parse(column);
            if (info.getOwner() == parent.from) {
                return super.accept(column, termType);
            }
            JoinConditionalSpecImpl join = parent.getJoinByClass(info.getOwner());
            return super.accept(join.alias + "." + info.getColumn(), termType, column.get());
        }

        @Override
        public <B> NestConditional<T> accept(StaticMethodReferenceColumn<B> column, String termType, Object value) {
            MethodReferenceInfo info = MethodReferenceConverter.parse(column);
            if (info.getOwner() == parent.from) {
                return super.accept(column, termType, value);
            }
            JoinConditionalSpecImpl join = parent.getJoinByClass(info.getOwner());

            return super.accept(join.alias + "." + info.getColumn(), termType, value);
        }

    }

    @AllArgsConstructor
    static class ConditionalImpl<T extends Conditional<T>> implements Conditional<T> {
        final QuerySpec<?> parent;

        final Conditional<T> real;

        @Override
        public NestConditional<T> nest() {
            Term term = new Term();
            term.setType(Term.Type.and);
            real.accept(term);

            return new NestConditionalImpl<>(parent, (T) this, term);
        }

        @Override
        public NestConditional<T> orNest() {
            Term term = new Term();
            term.setType(Term.Type.or);
            real.accept(term);
            return new NestConditionalImpl<>(parent, (T) this, term);
        }

        @Override
        public T and() {
            real.and();
            return castSelf();
        }

        @Override
        public T or() {
            real.or();
            return castSelf();
        }

        @Override
        public T and(String column, String termType, Object value) {
            real.and(column, termType, value);
            return castSelf();
        }

        @Override
        public T or(String column, String termType, Object value) {
            real.or(column, termType, value);
            return castSelf();
        }

        @Override
        public <B> T accept(MethodReferenceColumn<B> column, String termType) {
            MethodReferenceInfo info = MethodReferenceConverter.parse(column);
            if (info.getOwner() == parent.from) {
                return Conditional.super.accept(column, termType);
            }
            JoinConditionalSpecImpl join = parent.getJoinByClass(info.getOwner());

            return Conditional.super.accept(join.alias + "." + info.getColumn(), termType, column.get());
        }

        @Override
        public <B> T accept(StaticMethodReferenceColumn<B> column, String termType, Object value) {
            MethodReferenceInfo info = MethodReferenceConverter.parse(column);
            if (info.getOwner() == parent.from) {
                return Conditional.super.accept(column, termType, value);
            }
            JoinConditionalSpecImpl join = parent.getJoinByClass(info.getOwner());

            return Conditional.super.accept(join.alias + "." + info.getColumn(), termType, value);
        }

        @Override
        public Accepter<T, Object> getAccepter() {
            return real.getAccepter();
        }

        @Override
        public T accept(Term term) {
            real.accept(term);
            return castSelf();
        }
    }
}
