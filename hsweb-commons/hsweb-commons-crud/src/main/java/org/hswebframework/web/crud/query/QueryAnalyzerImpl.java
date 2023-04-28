package org.hswebframework.web.crud.query;

import lombok.SneakyThrows;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import org.hswebframework.ezorm.core.meta.FeatureSupportedMetadata;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBSchemaMetadata;
import org.hswebframework.ezorm.rdb.metadata.dialect.Dialect;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.AbstractTermsFragmentBuilder;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.EmptySqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.PrepareSqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.SqlFragments;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.springframework.util.StringUtils;

import java.util.*;

import static net.sf.jsqlparser.statement.select.PlainSelect.getStringList;
import static net.sf.jsqlparser.statement.select.PlainSelect.orderByToString;
import static org.hswebframework.ezorm.rdb.operator.builder.fragments.TermFragmentBuilder.createFeatureId;


class QueryAnalyzerImpl implements FromItemVisitor, SelectItemVisitor, SelectVisitor, QueryAnalyzer {

    private final DatabaseOperator database;

    private String sql;

    private final SelectBody parsed;

    private QueryAnalyzer.Select select;

    private final Map<String, QueryAnalyzer.Join> joins = new LinkedHashMap<>();

    private QueryInjector injector;

    @Override
    public String nativeSql() {
        return sql;
    }

    @Override
    public SqlRequest refactor(QueryParamEntity entity, Object... args) {
        if (injector == null) {
            initInjector();
        }
        return injector.inject(entity, args);
    }

    @Override
    public SqlRequest refactorCount(QueryParamEntity entity, Object... args) {
        if (injector == null) {
            initInjector();
        }
        return injector.injectCount(entity, args);
    }

    @Override
    public Select select() {
        return select;
    }

    @Override
    public List<Join> joins() {
        return new ArrayList<>(joins.values());
    }

    QueryAnalyzerImpl(DatabaseOperator database, String sql) {
        this(database, parse(sql));
        this.sql = sql;
    }

    @SneakyThrows
    private static SelectBody parse(String sql) {
        return ((net.sf.jsqlparser.statement.select.Select) CCJSqlParserUtil.parse(sql)).getSelectBody();
    }

    QueryAnalyzerImpl(DatabaseOperator database, SelectBody selectBody) {
        this.database = database;
        if (null != selectBody) {
            this.parsed = selectBody;
            selectBody.accept(this);
        } else {
            this.parsed = null;
        }
    }

    private String parsePlainName(String name) {
        if (null == name) {
            return null;
        }

        if (name.startsWith(database.getMetadata().getDialect().getQuoteStart())
                && name.endsWith(database.getMetadata().getDialect().getQuoteEnd())) {
            return name.substring(1, name.length() - 1);
        }
        return name;
    }

    @Override
    public void visit(net.sf.jsqlparser.schema.Table tableName) {
        String schema = parsePlainName(tableName.getSchemaName());
        RDBSchemaMetadata schemaMetadata;
        if (schema != null) {
            schemaMetadata = database
                    .getMetadata()
                    .getSchema(schema)
                    .orElseThrow(() -> new IllegalStateException("schema " + schema + " not initialized"));
        } else {
            schemaMetadata = database.getMetadata().getCurrentSchema();
        }

        String alias = tableName.getAlias() == null ? tableName.getName() : tableName.getAlias().getName();

        QueryAnalyzer.Table table = new QueryAnalyzer.Table(
                parsePlainName(alias),
                schemaMetadata
                        .getTableOrView(parsePlainName(tableName.getName()), false)
                        .orElseThrow(() -> new IllegalStateException("table or view " + tableName.getName() + " not found in " + schemaMetadata.getName()))
        );

        select = new QueryAnalyzer.Select(new LinkedHashMap<>(), table);

    }

    // select * from ( select a,b,c from table ) t
    @Override
    public void visit(SubSelect subSelect) {
        SelectBody body = subSelect.getSelectBody();
        QueryAnalyzerImpl sub = new QueryAnalyzerImpl(database, body);
        String alias = subSelect.getAlias() == null ? null : subSelect.getAlias().getName();

        Map<String, Column> columnMap = new LinkedHashMap<>();
        for (Map.Entry<String, Column> entry : sub.select.columns.entrySet()) {
            Column val = entry.getValue();

            columnMap.put(entry.getKey(),
                          new Column(val.alias, val.getAlias(), val.owner, val.metadata));
        }

        select = new QueryAnalyzer.Select(
                new LinkedHashMap<>(),
                new QueryAnalyzer.SelectTable(
                        parsePlainName(alias),
                        columnMap,
                        sub.select.table.metadata
                )
        );
    }

    @Override
    public void visit(SubJoin subjoin) {
        for (net.sf.jsqlparser.statement.select.Join join : subjoin.getJoinList()) {
            join.getRightItem().accept(this);
        }
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {

    }

    @Override
    public void visit(ValuesList valuesList) {

    }

    @Override
    public void visit(TableFunction tableFunction) {

    }

    @Override
    public void visit(ParenthesisFromItem aThis) {

    }

    @Override
    public void visit(AllColumns allColumns) {
        putSelectColumns(select.table, select.columns);

        for (QueryAnalyzer.Join value : new HashSet<>(joins.values())) {
            putSelectColumns(value.table, select.columns);
        }
    }

    private void putSelectColumns(String prefix, QueryAnalyzer.Table table, Map<String, QueryAnalyzer.Column> container) {

        if (table instanceof QueryAnalyzer.SelectTable) {
            QueryAnalyzer.SelectTable selectTable = ((QueryAnalyzer.SelectTable) table);

            for (QueryAnalyzer.Column column : selectTable.columns.values()) {
                container.put(column.getAlias(),
                              new QueryAnalyzer.Column(
                                      column.name,
                                      column.getAlias(),
                                      table.alias,
                                      column.metadata
                              ));
            }
        } else {
            for (RDBColumnMetadata column : table.metadata.getColumns()) {
                container.put(column.getAlias(),
                              new QueryAnalyzer.Column(
                                      column.getName(),
                                      column.getAlias(),
                                      table.alias,
                                      column
                              ));
            }
        }
    }

    private void putSelectColumns(QueryAnalyzer.Table table, Map<String, QueryAnalyzer.Column> container) {
        putSelectColumns(null, table, container);
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        net.sf.jsqlparser.schema.Table table = allTableColumns.getTable();

        QueryAnalyzer.Join join = joins.get(parsePlainName(table.getName()));

        if (join == null) {
            throw new IllegalStateException("table " + table.getName() + " not found in join");
        }
        putSelectColumns(join.table, select.columns);
    }

    private QueryAnalyzer.Table getTable(net.sf.jsqlparser.schema.Table table) {
        QueryAnalyzer.Table meta;
        if (null == table) {
            return select.table;
        }
        String tableName = parsePlainName(table.getName());

        if (Objects.equals(tableName, select.table.alias)) {
            meta = select.table;
        } else {
            QueryAnalyzer.Join join = joins.get(tableName);
            if (join == null) {
                throw new IllegalStateException("table " + table + " not found in from or join");
            }
            meta = join.table;
        }
        return meta;
    }


    static class ExpressionColumn extends Column {

        private final SelectItem expr;

        public ExpressionColumn(String alias, String owner, RDBColumnMetadata metadata, SelectItem expr) {
            super(alias, alias, owner, metadata);
            this.expr = expr;
        }
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        Expression expr = selectExpressionItem.getExpression();
        Alias alias = selectExpressionItem.getAlias();

        if (!(expr instanceof net.sf.jsqlparser.schema.Column)) {
            String aliasName = alias == null ? expr.toString() : alias.getName();
            select.columns.put(aliasName, new ExpressionColumn(aliasName, null, null, selectExpressionItem));

            return;
        }
        net.sf.jsqlparser.schema.Column column = ((net.sf.jsqlparser.schema.Column) expr);

        String columnName = parsePlainName(column.getColumnName());

        QueryAnalyzer.Table table = getTable(column.getTable());

        String aliasName = alias == null ? columnName : alias.getName();

        RDBColumnMetadata metadata = table
                .getMetadata()
                .getColumn(columnName)
                .orElse(null);

        if (metadata == null) {
            if (table instanceof QueryAnalyzer.SelectTable) {
                Column c = ((SelectTable) table).columns.get(columnName);
                if (null != c) {
                    metadata = c.metadata;
                }
            }
        }

        if (metadata == null) {
            throw new IllegalStateException("column [" + column.getColumnName() + "] not found in " + table.metadata.getName());
        }

        select.columns.put(aliasName, new QueryAnalyzer.Column(metadata.getName(), aliasName, table.alias, metadata));


    }

    @Override
    public void visit(PlainSelect select) {

        FromItem from = select.getFromItem();

        if (from == null) {
            throw new IllegalArgumentException("select can not be without 'from'");
        }
        from.accept(this);


        List<net.sf.jsqlparser.statement.select.Join> joinList = select.getJoins();

        if (joinList != null) {
            for (net.sf.jsqlparser.statement.select.Join join : joinList) {
                FromItem fromItem = join.getRightItem();
                QueryAnalyzerImpl joinAn = new QueryAnalyzerImpl(database, (SelectBody) null);
                fromItem.accept(joinAn);

                Join.Type type;
                if (join.isLeft()) {
                    type = Join.Type.left;
                } else if (join.isRight()) {
                    type = Join.Type.right;
                } else if (join.isInner()) {
                    type = Join.Type.inner;
                } else {
                    type = null;
                }
                joins.put(joinAn.select.table.alias, new Join(joinAn.select.table.alias, type, joinAn.select.table));
            }
        }

        for (SelectItem selectItem : select.getSelectItems()) {
            selectItem.accept(this);
        }
    }

    @Override
    public void visit(SetOperationList setOpList) {
        //union

        for (SelectBody body : setOpList.getSelects()) {
            body.accept(this);
            break;
        }


    }

    @Override
    public void visit(WithItem withItem) {

    }

    @Override
    public void visit(ValuesStatement aThis) {

    }

    private void initInjector() {
        SimpleQueryInjector injector = new SimpleQueryInjector();
        parsed.accept(injector);

        this.injector = injector;
    }

    static class QueryAnalyzerTermsFragmentBuilder extends AbstractTermsFragmentBuilder<QueryAnalyzerImpl> {

        @Override
        public SqlFragments createTermFragments(QueryAnalyzerImpl parameter, List<Term> terms) {
            return super.createTermFragments(parameter, terms);
        }

        @Override
        public SqlFragments createTermFragments(QueryAnalyzerImpl parameter, Term term) {
            String column = term.getColumn();
            String alias;

            Table table;
            String columnName = column;

            if (column.contains(".")) {
                String[] split = column.split("[.]");
                alias = split[0];
                columnName = split[1];
                if (Objects.equals(parameter.select.table.alias, alias)) {
                    table = parameter.select.table;
                } else {
                    QueryAnalyzer.Join join = parameter.joins.get(alias);
                    if (null != join) {
                        table = join.table;
                    } else {
                        throw new IllegalArgumentException("undefined column [" + column + "]");
                    }
                }

            } else {
                table = parameter.select.table;
                alias = parameter.select.table.alias;
            }

            if (table instanceof SelectTable) {
                SelectTable sTable = ((SelectTable) table);
                Column c = sTable.columns.get(columnName);
                if (c == null) {
                    return EmptySqlFragments.INSTANCE;
                }
                FeatureSupportedMetadata metadata = c.metadata;
                if (c.metadata == null) {
                    metadata = table.metadata;
                }
                return metadata
                        .findFeature(createFeatureId(term.getTermType()))
                        .map(feature -> feature.createFragments(sTable.alias + "." + c.name, c.metadata, term))
                        .orElse(EmptySqlFragments.INSTANCE);
            }

            return table
                    .metadata
                    .getColumn(columnName)
                    .flatMap(metadata -> metadata
                            .findFeature(createFeatureId(term.getTermType()))
                            .map(feature -> feature.createFragments(metadata.getFullName(alias), metadata, term)))
                    .orElse(EmptySqlFragments.INSTANCE);
        }
    }

    static QueryAnalyzerTermsFragmentBuilder TERMS_BUILDER = new QueryAnalyzerTermsFragmentBuilder();

    class SimpleQueryInjector implements QueryInjector, SelectVisitor {
        private String from;

        private String columns;

        private String where;

        private String orderBy;

        private String suffix;

        private boolean fastCount = true;

        public SimpleQueryInjector() {

        }


        private void initColumns(StringBuilder columns) {
            int idx = 0;
            Dialect dialect = database.getMetadata().getDialect();


            for (Map.Entry<String, Column> entry : select.columns.entrySet()) {
                if (idx++ > 0) {
                    columns.append(",");
                }
                Column column = entry.getValue();
                if (column instanceof ExpressionColumn) {
                    columns.append(((ExpressionColumn) column).expr);
                    fastCount = false;
                    continue;
                }
//                RDBColumnMetadata column=entry.getValue().metadata;
                boolean sameTable = Objects.equals(column.owner, select.table.alias);

                String columnName = column.owner + "." + dialect.quote(column.name);

                columns.append(columnName)
                       .append(" as ")
                       .append(sameTable
                                       ? dialect.quote(column.alias, false)
                                       : dialect.quote(column.owner + "." + column.alias, false));
            }
        }

        @Override
        public void visit(PlainSelect plainSelect) {

            StringBuilder from = new StringBuilder();
            StringBuilder columns = new StringBuilder();
            StringBuilder suffix = new StringBuilder();


            if (plainSelect.getDistinct() != null) {
                columns.append(plainSelect.getDistinct());
                fastCount = false;
            }

            initColumns(columns);

            //  prefix.append(getStringList(plainSelect.getSelectItems()));

            if (null != plainSelect.getFromItem()) {
                from.append("FROM ");

                from.append(plainSelect.getFromItem());
            }

            if (plainSelect.getJoins() != null) {
                for (net.sf.jsqlparser.statement.select.Join join : plainSelect.getJoins()) {
                    if (join.isSimple()) {
                        from.append(", ").append(join);
                    } else {
                        from.append(" ").append(join);
                    }
                }
            }

            if (null != plainSelect.getWhere()) {
                where = plainSelect.getWhere().toString();
            }

            if (plainSelect.getOrderByElements() != null) {
                orderBy = orderByToString(plainSelect.isOracleSiblings(), plainSelect.getOrderByElements());
            }

            if (null != plainSelect.getGroupBy()) {
                fastCount = false;
                suffix.append(' ').append(plainSelect.getGroupBy());
            }

            suffix.append(' ');
            if (null != plainSelect.getHaving()) {
                suffix.append(" HAVING ").append(plainSelect.getHaving());
            }

//            if (plainSelect.getLimit() != null) {
//                suffix.append(plainSelect.getLimit());
//            }
//            if (plainSelect.getOffset() != null) {
//                suffix.append(plainSelect.getOffset());
//            }

            this.columns = columns.toString();
            this.from = from.toString();
            this.suffix = suffix.toString();

        }

        @Override
        public void visit(SetOperationList setOpList) {
            StringBuilder from = new StringBuilder();
            StringBuilder columns = new StringBuilder();
          //  StringBuilder suffix = new StringBuilder();

            initColumns(columns);

            from.append("from (");
            from.append(setOpList);
            from.append(") ");
            from.append(select.table.alias);

            this.from = from.toString();
            this.columns = columns.toString();
            this.suffix = "";

        }

        @Override
        public void visit(WithItem withItem) {

        }

        @Override
        public void visit(ValuesStatement aThis) {

        }

        @Override
        public SqlRequest inject(QueryParamEntity param, Object... args) {
            PrepareSqlFragments sql = PrepareSqlFragments
                    .of("select", args)
                    .addSql(columns)
                    .addSql(from);

            appendWhere(sql, param);

            appendOrderBy(sql, param);

            sql.addSql(suffix);

            return sql.toRequest();
        }


        @Override
        public SqlRequest injectCount(QueryParamEntity param, Object... args) {
            PrepareSqlFragments sql = PrepareSqlFragments.of("select", args);

            if (fastCount) {
                sql.addSql("count(1) as _total");

                sql.addSql(from);

                appendWhere(sql, param);

                sql.addSql(suffix);
            } else {
                sql.addSql("count(1) as _total from (select")
                   .addSql(columns, from);

                appendWhere(sql, param);

                sql.addSql(suffix)
                   .addSql(")");
            }


            return sql.toRequest();
        }


        private void appendOrderBy(PrepareSqlFragments sql, QueryParamEntity param) {
            if (orderBy != null) {
                sql.addSql(orderBy);
            }
        }

        private void appendWhere(PrepareSqlFragments sql, QueryParamEntity param) {
            SqlFragments fragments = TERMS_BUILDER.createTermFragments(QueryAnalyzerImpl.this, param.getTerms());

            if (fragments.isNotEmpty() || StringUtils.hasText(where)) {
                sql.addSql(" WHERE ");
            }

            if (StringUtils.hasText(where)) {
                sql.addSql("(");
                sql.addSql(where);
                sql.addSql(")");
            }

            if (fragments.isNotEmpty()) {
                if (StringUtils.hasText(where)) {
                    sql.addSql("AND");
                }
                sql.addSql("(");
                sql.addFragments(fragments);
                sql.addSql(")");
            }
        }

    }

    private interface QueryInjector {

        SqlRequest inject(QueryParamEntity param, Object... args);

        SqlRequest injectCount(QueryParamEntity param, Object... args);
    }

}
