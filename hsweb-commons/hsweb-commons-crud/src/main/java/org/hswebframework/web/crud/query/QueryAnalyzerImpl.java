package org.hswebframework.web.crud.query;

import lombok.Getter;
import lombok.SneakyThrows;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.ezorm.core.meta.FeatureSupportedMetadata;
import org.hswebframework.ezorm.core.param.Sort;
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
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;

import static net.sf.jsqlparser.statement.select.PlainSelect.getFormatedList;
import static org.hswebframework.ezorm.rdb.operator.builder.fragments.TermFragmentBuilder.createFeatureId;


class QueryAnalyzerImpl implements FromItemVisitor, SelectItemVisitor, SelectVisitor, QueryAnalyzer {

    private final DatabaseOperator database;

    private String sql;

    private final SelectBody parsed;

    private QueryAnalyzer.Select select;

    private final Map<String, QueryAnalyzer.Join> joins = new LinkedHashMap<>();

    private QueryRefactor injector;

    private volatile Map<String, Column> columnMappings;

    @Override
    public String originalSql() {
        return sql;
    }

    @Override
    public SqlRequest refactor(QueryParamEntity entity, Object... args) {
        if (injector == null) {
            initInjector();
        }
        return injector.refactor(entity, args);
    }

    @Override
    public SqlRequest refactorCount(QueryParamEntity entity, Object... args) {
        if (injector == null) {
            initInjector();
        }
        return injector.refactorCount(entity, args);
    }

    @Override
    public Select select() {
        return select;
    }

    @Override
    public Optional<Column> findColumn(String name) {
        return Optional.ofNullable(getColumnMappings().get(name));
    }

    @Override
    public List<Join> joins() {
        return new ArrayList<>(joins.values());
    }

    QueryAnalyzerImpl(DatabaseOperator database, String sql) {
        this(database, parse(sql));
        this.sql = sql;
    }

    private Map<String, Column> getColumnMappings() {
        if (columnMappings == null) {
            synchronized (this) {
                if (columnMappings == null) {
                    columnMappings = new HashMap<>();
                    // 主表
                    for (RDBColumnMetadata column : select.table.metadata.getColumns()) {
                        Column col = new Column(column.getName(), column.getAlias(), select.table.alias, column);
                        columnMappings.put(column.getName(), col);
                        columnMappings.put(column.getAlias(), col);
                        columnMappings.put(select.table.alias + "." + column.getName(), col);
                        columnMappings.put(select.table.alias + "." + column.getAlias(), col);
                    }
                    //关联表
                    for (Join join : joins.values()) {
                        for (RDBColumnMetadata column : join.table.metadata.getColumns()) {
                            Column col = new Column(column.getName(), column.getAlias(), join.alias, column);
                            columnMappings.putIfAbsent(column.getName(), col);
                            columnMappings.putIfAbsent(column.getAlias(), col);

                            columnMappings.put(join.alias + "." + column.getName(), col);
                            columnMappings.put(join.alias + "." + column.getAlias(), col);
                        }
                    }
                }
            }
        }
        return columnMappings;
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

        select = new QueryAnalyzer.Select(new ArrayList<>(), table);

    }

    // select * from ( select a,b,c from table ) t
    @Override
    public void visit(SubSelect subSelect) {
        SelectBody body = subSelect.getSelectBody();
        QueryAnalyzerImpl sub = new QueryAnalyzerImpl(database, body);
        String alias = subSelect.getAlias() == null ? null : subSelect.getAlias().getName();

        Map<String, Column> columnMap = new LinkedHashMap<>();
        for (Column column : sub.select.getColumnList()) {

            columnMap.put(column.getAlias(),
                          new Column(column.alias, column.getAlias(), column.owner, column.metadata));
        }

        select = new QueryAnalyzer.Select(
                new ArrayList<>(),
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
        putSelectColumns(select.table, select.columnList);

        for (QueryAnalyzer.Join value : new HashSet<>(joins.values())) {
            putSelectColumns(value.table, select.columnList);
        }
    }

    private void putSelectColumns(QueryAnalyzer.Table table, List<QueryAnalyzer.Column> container) {

        if (table instanceof QueryAnalyzer.SelectTable) {
            QueryAnalyzer.SelectTable selectTable = ((QueryAnalyzer.SelectTable) table);

            for (QueryAnalyzer.Column column : selectTable.columns.values()) {
                container.add(new QueryAnalyzer.Column(
                        column.name,
                        column.getAlias(),
                        table.alias,
                        column.metadata
                ));
            }
        } else {
            for (RDBColumnMetadata column : table.metadata.getColumns()) {
                container.add(new QueryAnalyzer.Column(
                        column.getName(),
                        column.getAlias(),
                        table.alias,
                        column
                ));
            }
        }
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        net.sf.jsqlparser.schema.Table table = allTableColumns.getTable();

        String name = table.getName();

        if (Objects.equals(select.table.alias, name)) {
            putSelectColumns(select.table, select.columnList);
            return;
        }

        QueryAnalyzer.Join join = joins.get(parsePlainName(table.getName()));

        if (join == null) {
            throw new IllegalStateException("table " + table.getName() + " not found in join");
        }
        putSelectColumns(join.table, select.columnList);
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
            select.columnList.add(new ExpressionColumn(aliasName, null, null, selectExpressionItem));

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

        select.columnList.add(new QueryAnalyzer.Column(metadata.getName(), aliasName, table.alias, metadata));


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
        SimpleQueryRefactor injector = new SimpleQueryRefactor();
        parsed.accept(injector);

        this.injector = injector;
    }

    static class QueryAnalyzerTermsFragmentBuilder extends AbstractTermsFragmentBuilder<QueryAnalyzerImpl> {

        @Override
        public SqlFragments createTermFragments(QueryAnalyzerImpl parameter, List<Term> terms) {
            return super.createTermFragments(parameter, terms);
        }

        @Override
        public SqlFragments createTermFragments(QueryAnalyzerImpl impl, Term term) {
            Dialect dialect = impl.database.getMetadata().getDialect();

            Table table;
            String column = term.getColumn();

            Column col = impl.getColumnMappings().get(column);

            if (col == null) {
                throw new IllegalArgumentException("undefined column [" + column + "]");
            }

            if (Objects.equals(impl.select.table.alias, col.getOwner())) {
                table = impl.select.table;
            } else {
                QueryAnalyzer.Join join = impl.joins.get(col.getOwner());
                if (null != join) {
                    table = join.table;
                } else {
                    throw new IllegalArgumentException("undefined column [" + column + "]");
                }
            }

            FeatureSupportedMetadata metadata = col.metadata;
            if (col.metadata == null) {
                metadata = table.metadata;
            }
            return metadata
                    .findFeature(createFeatureId(term.getTermType()))
                    .map(feature -> feature.createFragments(table.alias + "." + dialect.quote(col.name), col.metadata, term))
                    .orElse(EmptySqlFragments.INSTANCE);
        }
    }

    static QueryAnalyzerTermsFragmentBuilder TERMS_BUILDER = new QueryAnalyzerTermsFragmentBuilder();

    class SimpleQueryRefactor implements QueryRefactor, SelectVisitor {
        private String from;

        private String columns;

        private String where;
        private int prefixParameters;
        private String orderBy;

        private String suffix;
        private int suffixParameters;

        private boolean fastCount = true;

        SimpleQueryRefactor() {

        }


        private void initColumns(StringBuilder columns) {
            int idx = 0;
            Dialect dialect = database.getMetadata().getDialect();

            for (Column column : select.columnList) {
                if (idx++ > 0) {
                    columns.append(",");
                }
                if (column instanceof ExpressionColumn) {
                    columns.append(((ExpressionColumn) column).expr);
                    fastCount = false;
                    continue;
                }
                boolean sameTable = Objects.equals(column.owner, select.table.alias);

                columns.append(column.owner).append('.').append(dialect.quote(column.name))
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

            if (plainSelect.getFromItem() != null) {
                from.append("FROM ");

                from.append(plainSelect.getFromItem());
            }

            if (plainSelect.getJoins() != null) {
                PrepareStatementVisitor visitor = new PrepareStatementVisitor();
                for (net.sf.jsqlparser.statement.select.Join join : plainSelect.getJoins()) {
                    if (join.isSimple()) {
                        from.append(", ").append(join);
                    } else {
                        from.append(" ").append(join);
                    }
                    if (null != join.getOnExpressions()) {
                        for (Expression onExpression : join.getOnExpressions()) {
                            onExpression.accept(visitor);
                        }
                    }
                }
                prefixParameters += visitor.parameterSize;
            }

            if (plainSelect.getWhere() != null) {
                PrepareStatementVisitor visitor = new PrepareStatementVisitor();
                plainSelect.getWhere().accept(visitor);
                prefixParameters += visitor.parameterSize;
                where = plainSelect.getWhere().toString();
            }

            if (plainSelect.getOrderByElements() != null) {
                orderBy = getFormatedList(plainSelect.getOrderByElements(), "");
            }

            if (plainSelect.getGroupBy() != null) {
                fastCount = false;
                suffix.append(' ').append(plainSelect.getGroupBy());
            }
            suffix.append(' ');

            if (plainSelect.getHaving() != null) {
                PrepareStatementVisitor visitor = new PrepareStatementVisitor();
                plainSelect.getHaving().accept(visitor);
                suffixParameters = visitor.parameterSize;
                suffix.append(" HAVING ").append(plainSelect.getHaving());
            }

            this.columns = columns.toString();
            this.from = from.toString();
            this.suffix = suffix.toString();

        }

        @Override
        public void visit(SetOperationList setOpList) {
            StringBuilder from = new StringBuilder();
            StringBuilder columns = new StringBuilder();

            initColumns(columns);

            from.append("FROM (");
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

        public Object[] getPrefixParameters(Object... args) {
            if (prefixParameters == 0) {
                return new Object[0];
            }
            Assert.isTrue(args.length >= prefixParameters,
                          "Illegal prepare statement parameter size, expect: " + prefixParameters + ", actual: " + args.length);

            return Arrays.copyOfRange(args, 0, prefixParameters);
        }

        public Object[] getSuffixParameters(Object... args) {
            if (suffixParameters == 0) {
                return new Object[0];
            }
            Assert.isTrue(args.length >= suffixParameters + prefixParameters,
                          "Illegal prepare statement parameter size, expect: " + suffixParameters + prefixParameters + ", actual: " + args.length);

            return Arrays.copyOfRange(args, prefixParameters, suffixParameters + prefixParameters);
        }

        @Override
        public SqlRequest refactor(QueryParamEntity param, Object... args) {
            PrepareSqlFragments sql = PrepareSqlFragments
                    .of("SELECT")
                    .addSql(columns)
                    .addSql(from)
                    .addParameter(getPrefixParameters(args));

            appendWhere(sql, param);

            sql.addSql(suffix)
               .addParameter(getSuffixParameters(args));

            appendOrderBy(sql, param);

            return sql.toRequest();
        }

        @Override
        public SqlRequest refactorCount(QueryParamEntity param, Object... args) {
            PrepareSqlFragments sql = PrepareSqlFragments
                    .of("SELECT", getPrefixParameters(args));

            if (fastCount) {
                sql.addSql("count(1) as _total");

                sql.addSql(from);

                appendWhere(sql, param);

                sql.addSql(suffix);
            } else {
                sql.addSql("count(1) as _total from (SELECT")
                   .addSql(columns, from);

                appendWhere(sql, param);

                sql.addSql(suffix)
                   .addSql(")");
            }

            return sql
                    .addParameter(getSuffixParameters(args))
                    .toRequest();
        }

        private void appendOrderBy(PrepareSqlFragments sql, QueryParamEntity param) {

            if (CollectionUtils.isNotEmpty(param.getSorts())) {
                int index = 0;
                PrepareSqlFragments orderByValue = null;
                PrepareSqlFragments orderByColumn = null;
                for (Sort sort : param.getSorts()) {
                    String name = sort.getName();
                    Column column = getColumnMappings().get(name);

                    if (column == null) {
                        continue;
                    }
                    boolean desc = "desc".equalsIgnoreCase(sort.getOrder());
                    String columnName = org.hswebframework.ezorm.core.utils.StringUtils
                            .concat(column.getOwner(),
                                    ".",
                                    database.getMetadata().getDialect().quote(column.getName()));
                    //按固定值排序
                    if (sort.getValue() != null) {
                        if (orderByValue == null) {
                            orderByValue = PrepareSqlFragments.of();
                            orderByValue.addSql("case");
                        }
                        orderByValue.addSql("when");
                        orderByValue.addSql(columnName, "= ?").addParameter(sort.getValue());
                        orderByValue.addSql("then").addSql(String.valueOf(desc ? 10000 + index++ : index++));
                    } else {
                        if (orderByColumn == null) {
                            orderByColumn = PrepareSqlFragments.of();
                        } else {
                            orderByColumn.addSql(",");
                        }
                        //todo function支持
                        orderByColumn
                                .addSql(columnName)
                                .addSql(desc ? "DESC" : "ASC");
                    }
                }

                boolean customOrder = (orderByValue != null || orderByColumn != null);

                if (customOrder || orderBy != null) {
                    sql.addSql("ORDER BY");
                }
                //按固定值
                if (orderByValue != null) {
                    orderByValue.addSql("else 10000 end");
                    sql.addFragments(orderByValue);
                }
                //按列
                if (orderByColumn != null) {
                    if (orderByValue != null) {
                        sql.addSql(",");
                    }
                    sql.addFragments(orderByColumn);
                }
                if (orderBy != null) {
                    if (customOrder) {
                        sql.addSql(",");
                    }
                    sql.addSql(orderBy);
                }
            } else {
                if (orderBy != null) {
                    sql.addSql("ORDER BY", orderBy);
                }
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


    @Getter
    static class PrepareStatementVisitor extends ExpressionVisitorAdapter {
        private int parameterSize;

        @Override
        public void visit(JdbcParameter parameter) {
            parameterSize++;
            super.visit(parameter);
        }
    }

    private interface QueryRefactor {

        SqlRequest refactor(QueryParamEntity param, Object... args);

        SqlRequest refactorCount(QueryParamEntity param, Object... args);
    }

}
