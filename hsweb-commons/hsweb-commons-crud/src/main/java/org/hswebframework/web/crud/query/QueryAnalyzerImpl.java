package org.hswebframework.web.crud.query;

import lombok.Getter;
import lombok.SneakyThrows;
import net.sf.jsqlparser.Model;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.ezorm.core.meta.FeatureSupportedMetadata;
import org.hswebframework.ezorm.core.param.Sort;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.metadata.*;
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

    private final List<WithItem> withItems = new ArrayList<>();
    private QueryRefactor injector;

    private volatile Map<String, Column> columnMappings;

    private final Map<String, TableOrViewMetadata> virtualTable = new HashMap<>();

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


    public boolean columnIsExpression(String name, int index) {

        if (index >= 0 && select.getColumnList().size() > index) {
            return select.getColumnList().get(index) instanceof ExpressionColumn;
        }

        return select.getColumns().get(name) instanceof ExpressionColumn;
    }

    private Map<String, Column> getColumnMappings() {
        if (columnMappings == null) {
            synchronized (this) {
                if (columnMappings == null) {
                    columnMappings = new HashMap<>();

                    if (select.table instanceof SelectTable) {

                        for (Map.Entry<String, Column> entry :
                                ((SelectTable) select.getTable()).getColumns().entrySet()) {
                            Column column = entry.getValue();
                            Column col = new Column(column.getName(), column.getAlias(), select.table.alias, column.metadata);
                            columnMappings.put(entry.getKey(), col);
                            columnMappings.put(select.table.alias + "." + entry.getKey(), col);

                            if (!(column instanceof ExpressionColumn) && column.metadata != null) {
                                columnMappings.put(column.metadata.getName(), col);
                                columnMappings.put(select.table.alias + "." + column.metadata.getName(), col);
                                columnMappings.put(column.metadata.getAlias(), col);
                                columnMappings.put(select.table.alias + "." + column.metadata.getAlias(), col);
                            }
                        }

                        for (Column column : select.getColumnList()) {
                            columnMappings.put(column.getName(), column);
                            columnMappings.put(column.getAlias(), column);
                            if (null != column.getOwner()) {
                                columnMappings.put(column.getOwner() + "." + column.getName(), column);
                                columnMappings.put(column.getOwner() + "." + column.getAlias(), column);
                            }
                        }
                    } else {
                        // 主表
                        for (RDBColumnMetadata column : select.table.metadata.getColumns()) {
                            Column col = new Column(column.getName(), column.getAlias(), select.table.alias, column);
                            columnMappings.put(column.getName(), col);
                            columnMappings.put(column.getAlias(), col);
                            columnMappings.put(select.table.alias + "." + column.getName(), col);
                            columnMappings.put(select.table.alias + "." + column.getAlias(), col);
                        }
                    }

                    //关联表
                    for (Join join : joins.values()) {
                        if (join.table instanceof SelectTable) {
                            for (Column column : select.getColumnList()) {
                                columnMappings.putIfAbsent(column.getName(), column);
                                columnMappings.putIfAbsent(column.getAlias(), column);
                                columnMappings.put(column.getOwner() + "." + column.getName(), column);
                                columnMappings.put(column.getOwner() + "." + column.getAlias(), column);
                            }
                        } else {
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
        }
        return columnMappings;
    }

    private Column getColumnOrSelectColumn(String name) {
        Column column = select.getColumns().get(name);

        if (column != null) {
            return column;
        }
        column = select.getColumns().get(QueryHelperUtils.toSnake(name));
        if (column != null) {
            return column;
        }

        return getColumnMappings().get(name);
    }

    @SneakyThrows
    private static net.sf.jsqlparser.statement.select.Select parse(String sql) {
        return ((net.sf.jsqlparser.statement.select.Select) CCJSqlParserUtil.parse(sql));
    }

    QueryAnalyzerImpl(DatabaseOperator database, SelectBody selectBody, QueryAnalyzerImpl parent) {
        this.database = database;
        this.virtualTable.putAll(parent.virtualTable);
        if (null != selectBody) {
            this.parsed = selectBody;
            selectBody.accept(this);
        } else {
            this.parsed = null;
        }
    }

    QueryAnalyzerImpl(DatabaseOperator database, SubSelect select, QueryAnalyzerImpl parent) {
        this.parsed = select.getSelectBody();
        this.database = database;
        this.virtualTable.putAll(parent.virtualTable);
        //with ...
        if (CollectionUtils.isNotEmpty(select.getWithItemsList())) {
            for (WithItem withItem : select.getWithItemsList()) {
                withItem.accept(this);
            }
        }
        if (this.parsed != null) {
            this.parsed.accept(this);
        }
    }

    QueryAnalyzerImpl(DatabaseOperator database, net.sf.jsqlparser.statement.select.Select select) {
        this.parsed = select.getSelectBody();
        this.database = database;
        //with ...
        if (CollectionUtils.isNotEmpty(select.getWithItemsList())) {
            for (WithItem withItem : select.getWithItemsList()) {
                withItem.accept(this);
            }
        }

        if (this.parsed != null) {
            this.parsed.accept(this);
        }
    }

    private String parsePlainName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        char firstChar = name.charAt(0);

        if (firstChar == '`' || firstChar == '"' || firstChar == '[' ||
                name.startsWith(database.getMetadata().getDialect().getQuoteStart())) {

            return new String(name.toCharArray(), 1, name.length() - 2);
        }

        return name;
    }

    @Override
    public void visit(net.sf.jsqlparser.schema.Table tableName) {
        String schema = parsePlainName(tableName.getSchemaName());

        String name = parsePlainName(tableName.getName());

        RDBSchemaMetadata schemaMetadata;
        if (schema != null) {
            schemaMetadata = database
                    .getMetadata()
                    .getSchema(schema)
                    .orElseThrow(() -> new IllegalStateException("schema " + schema + " not initialized"));
        } else {
            schemaMetadata = database.getMetadata().getCurrentSchema();
            if (!virtualTable.containsKey(name)) {
                tableName.setSchemaName(schemaMetadata.getName());
            }
        }

        String alias = tableName.getAlias() == null ? tableName.getName() : tableName.getAlias().getName();

        TableOrViewMetadata tableMetadata = schemaMetadata
                .getTableOrView(name, false)
                .orElseGet(() -> virtualTable.get(name));

        if (tableMetadata == null) {
            throw new IllegalStateException("table or view " + tableName.getName() + " not found in " + schemaMetadata.getName());
        }

        QueryAnalyzer.Table table = new QueryAnalyzer.Table(
                parsePlainName(alias),
                tableMetadata
        );

        select = new QueryAnalyzer.Select(new ArrayList<>(), table);

    }

    // select * from ( select a,b,c from table ) t
    @Override
    public void visit(SubSelect subSelect) {
        SelectBody body = subSelect.getSelectBody();
        QueryAnalyzerImpl sub = new QueryAnalyzerImpl(database, body, this);
        String alias = subSelect.getAlias() == null ? null : subSelect.getAlias().getName();

        Map<String, Column> columnMap = new LinkedHashMap<>();
        for (Column column : sub.select.getColumnList()) {

            columnMap.put(column.getAlias(),
                          new Column(column.name, column.getAlias(), column.owner, column.metadata));
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
                String alias = table == select.table ? column.getAlias() : table.alias + "." + column.getAlias();
                container.add(new QueryAnalyzer.Column(
                        column.name,
                        alias,
                        table.alias,
                        column.metadata
                ));
            }
        } else {
            for (RDBColumnMetadata column : table.metadata.getColumns()) {
                String alias = table == select.table ? column.getAlias() : table.alias + "." + column.getAlias();

                container.add(new QueryAnalyzer.Column(
                        column.getName(),
                        alias,
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

    private void refactorAlias(Alias alias) {
        if (alias != null) {
            alias.setName(
                    database
                            .getMetadata()
                            .getDialect()
                            .quote(parsePlainName(alias.getName()), false)
            );
        }
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        Expression expr = selectExpressionItem.getExpression();
        Alias alias = selectExpressionItem.getAlias();

        if (!(expr instanceof net.sf.jsqlparser.schema.Column)) {
            String aliasName = parsePlainName(alias == null ? expr.toString() : alias.getName());
            refactorAlias(alias);
            select.columnList.add(new ExpressionColumn(aliasName, null, null, selectExpressionItem));

            return;
        }
        net.sf.jsqlparser.schema.Column column = ((net.sf.jsqlparser.schema.Column) expr);

        String columnName = parsePlainName(column.getColumnName());

        QueryAnalyzer.Table table = getTable(column.getTable());

        String aliasName = alias == null ? columnName : parsePlainName(alias.getName());

        RDBColumnMetadata metadata = table
                .getMetadata()
                .getColumn(columnName)
                .orElse(null);

        if (metadata == null) {
            if (table instanceof QueryAnalyzer.SelectTable) {
                Column c = ((SelectTable) table).columns.get(columnName);
                if (null != c) {
                    if (c.metadata == null) {
                        select.columnList.add(new QueryAnalyzer.Column(c.getName(), aliasName, table.alias, null));
                        return;
                    }
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
                QueryAnalyzerImpl joinAn = new QueryAnalyzerImpl(database, (SelectBody) null, this);
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
            // break;
        }


    }

    @Override
    public void visit(WithItem withItem) {
        withItems.add(withItem);

        String name = withItem.getName();
        RDBViewMetadata view = new RDBViewMetadata();
        view.setName(name);
        view.setSchema(database.getMetadata().getCurrentSchema());
        virtualTable.put(name, view);
        if (withItem.getSubSelect() != null) {
            QueryAnalyzerImpl analyzer = new QueryAnalyzerImpl(database, withItem.getSubSelect(), this);
            for (Column column : analyzer.select.getColumnList()) {
                RDBColumnMetadata metadata;
                if (column.getMetadata() == null) {
                    metadata = new RDBColumnMetadata();
                } else {
                    metadata = column.metadata.clone();
                }
                metadata.setName(column.getName());
                metadata.setAlias(column.getAlias());
                view.addColumn(metadata);
            }
        }
    }

    @Override
    public void visit(ValuesStatement aThis) {

    }

    private void initInjector() {
        SimpleQueryRefactor injector = new SimpleQueryRefactor();
        parsed.accept(injector);
        for (WithItem withItem : withItems) {
            withItem.accept(injector);
        }
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
            String colName = col.metadata != null ? col.metadata.getName() : col.name;
            return metadata
                    .findFeature(createFeatureId(term.getTermType()))
                    .map(feature -> feature.createFragments(
                            table.alias + "." + dialect.quote(colName, col.metadata != null), col.metadata, term))
                    .orElse(EmptySqlFragments.INSTANCE);
        }
    }

    static QueryAnalyzerTermsFragmentBuilder TERMS_BUILDER = new QueryAnalyzerTermsFragmentBuilder();

    class SimpleQueryRefactor implements QueryRefactor, SelectVisitor {
        private String prefix = "";
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

                columns.append(column.owner).append('.').append(dialect.quote(column.name, column.metadata != null))
                       .append(" as ")
                       .append(dialect.quote(column.alias, false));
            }
        }

        @Override
        public void visit(PlainSelect plainSelect) {

            StringBuilder from = new StringBuilder();
            StringBuilder columns = new StringBuilder();
            StringBuilder suffix = new StringBuilder();


            if (plainSelect.getDistinct() != null) {
                columns.append(plainSelect.getDistinct())
                    .append(' ');
                fastCount = false;
            }

            initColumns(columns);

            if (plainSelect.getFromItem() != null) {
                from.append("FROM ");

                from.append(plainSelect.getFromItem());
                PrepareStatementVisitor visitor = new PrepareStatementVisitor();
                plainSelect.getFromItem().accept(visitor);
                prefixParameters += visitor.parameterSize;
            }

            if (plainSelect.getJoins() != null) {
                PrepareStatementVisitor visitor = new PrepareStatementVisitor();
                for (net.sf.jsqlparser.statement.select.Join join : plainSelect.getJoins()) {
                    if (join.isSimple()) {
                        from.append(", ").append(join);
                    } else {
                        from.append(" ").append(join);
                    }
                    if (null != join.getRightItem()) {
                        join.getRightItem().accept(visitor);
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
            if (!StringUtils.hasText(prefix)) {
                prefix += "WITH ";
            }
            prefix += withItem;
            PrepareStatementVisitor visitor = new PrepareStatementVisitor();
            withItem.accept(visitor);
            prefixParameters += visitor.parameterSize;
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
                    .of(prefix)
                    .addSql("SELECT")
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
                    .of(prefix)
                    .addSql("SELECT")
                    .addParameter(getPrefixParameters(args));

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
                   .addSql(") _t");
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
                    Column column = getColumnOrSelectColumn(name);

                    if (column == null) {
                        continue;
                    }
                    boolean desc = "desc".equalsIgnoreCase(sort.getOrder());
                    String columnName = column.getOwner() == null ?
                            database.getMetadata().getDialect().quote(column.getName(), false)
                            : org.hswebframework.ezorm.core.utils.StringUtils
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
    static class PrepareStatementVisitor extends ExpressionVisitorAdapter implements FromItemVisitor, SelectVisitor {
        private int parameterSize;

        public PrepareStatementVisitor() {
            setSelectVisitor(this);
        }

        @Override
        public void visit(JdbcParameter parameter) {
            parameterSize++;
            super.visit(parameter);
        }

        @Override
        public void visit(net.sf.jsqlparser.schema.Table tableName) {

        }

        @Override
        public void visit(SubJoin subjoin) {
            if (subjoin.getLeft() != null) {
                subjoin.getLeft().accept(this);
            }
            if (CollectionUtils.isNotEmpty(subjoin.getJoinList())) {
                for (net.sf.jsqlparser.statement.select.Join join : subjoin.getJoinList()) {
                    if (join.getRightItem() != null) {
                        join.getRightItem().accept(this);
                    }
                    if (join.getOnExpressions() != null) {
                        join.getOnExpressions().forEach(expr -> expr.accept(this));
                    }
                }
            }
        }

        @Override
        public void visit(LateralSubSelect lateralSubSelect) {
            if (lateralSubSelect.getSubSelect() != null) {
                lateralSubSelect.getSubSelect().accept((ExpressionVisitor) this);
            }
        }

        @Override
        public void visit(ValuesList valuesList) {
            if (valuesList.getMultiExpressionList() != null) {
                for (ExpressionList expressionList : valuesList.getMultiExpressionList().getExpressionLists()) {
                    expressionList.getExpressions().forEach(expr -> expr.accept(this));
                }
            }
        }

        @Override
        public void visit(TableFunction tableFunction) {
            tableFunction.getFunction().accept(this);
        }

        @Override
        public void visit(ParenthesisFromItem aThis) {
            aThis.getFromItem().accept(this);
        }

        @Override
        public void visit(PlainSelect plainSelect) {
            plainSelect.getFromItem().accept(this);
            if (plainSelect.getJoins() != null) {
                for (net.sf.jsqlparser.statement.select.Join join : plainSelect.getJoins()) {
                    join.getRightItem().accept(this);
                }
            }
            if (plainSelect.getSelectItems() != null) {
                for (SelectItem selectItem : plainSelect.getSelectItems()) {
                    selectItem.accept(this);
                }
            }
            if (plainSelect.getWhere() != null) {
                plainSelect.getWhere().accept(this);
            }
            if (plainSelect.getHaving() != null) {
                plainSelect.getHaving().accept(this);
            }

            if (plainSelect.getGroupBy() != null) {
                for (Expression expression : plainSelect.getGroupBy().getGroupByExpressionList().getExpressions()) {
                    expression.accept(this);
                }
            }
        }

        @Override
        public void visit(SetOperationList setOpList) {
            if (CollectionUtils.isNotEmpty(setOpList.getSelects())) {
                for (SelectBody select : setOpList.getSelects()) {
                    select.accept(this);
                }
            }
            if (setOpList.getOffset() != null) {
                setOpList.getOffset().getOffset().accept(this);
            }
            if (setOpList.getLimit() != null) {
                if (setOpList.getLimit().getRowCount() != null) {
                    setOpList.getLimit().getRowCount().accept(this);
                }
                if (setOpList.getLimit().getOffset() != null) {
                    setOpList.getLimit().getOffset().accept(this);
                }
            }
        }

        @Override
        public void visit(WithItem withItem) {
            if (CollectionUtils.isNotEmpty(withItem.getWithItemList())) {
                for (SelectItem selectItem : withItem.getWithItemList()) {
                    selectItem.accept(this);
                }
            }
            if (withItem.getSubSelect() != null) {
                withItem.getSubSelect().accept((ExpressionVisitor) this);
            }
        }

        @Override
        public void visit(ValuesStatement aThis) {
            if (aThis.getExpressions() != null) {
                aThis.getExpressions().accept(this);
            }
        }
    }

    private interface QueryRefactor {

        SqlRequest refactor(QueryParamEntity param, Object... args);

        SqlRequest refactorCount(QueryParamEntity param, Object... args);
    }

}
