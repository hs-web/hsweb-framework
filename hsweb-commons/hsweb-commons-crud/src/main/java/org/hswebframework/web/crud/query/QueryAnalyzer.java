package org.hswebframework.web.crud.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.TableOrViewMetadata;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 查询分析器,用于分析SQL查询语句以及对SQL进行重构,追加查询条件等操作
 *
 * @author zhouhao
 * @since 4.0.16
 */
public interface QueryAnalyzer {

    /**
     * @return 原始SQL
     */
    String originalSql();

    /**
     * 基于{@link QueryParamEntity}动态条件来重构SQL,将根据动态条件添加where条件,排序等操作.
     *
     * @param entity 查询条件
     * @param args   原始SQL中的预编译参数
     * @return 重构后的SQL
     */
    SqlRequest refactor(QueryParamEntity entity, Object... args);

    /**
     * 基于{@link QueryParamEntity}动态条件来重构用于查询count的SQL,通常用于分页时查询总数.
     * <pre>{@code
     *  select count(1) _total from .....
     * }</pre>
     *
     * @param entity 查询条件
     * @param args   原始SQL中的预编译参数
     * @return 重构后的SQL
     */
    SqlRequest refactorCount(QueryParamEntity entity, Object... args);

    /**
     * @return 查询信息
     */
    Select select();

    /**
     * 根据名称或者别名,查找查询语句中的列信息.
     *
     * @param name 列名、别名或者列全名
     * @return 列信息
     */
    Optional<Column> findColumn(String name);

    /**
     * 判断查询的列是否为表达式,如使用了函数: sum(num) as num
     *
     * @param name  列名
     * @param index 列序号
     * @return 是否为表达式
     */
    boolean columnIsExpression(String name, int index);

    /**
     * @return 关联表信息
     */
    List<Join> joins();

    @AllArgsConstructor
    @Getter
    class Join {

        final String alias;
        final Type type;
        final Table table;

        //  final List<Term> on;

        enum Type {
            left, right, inner
        }
    }

    @RequiredArgsConstructor
    @Getter
    class Select {
        private transient Map<String, Column> columns;

        final List<Column> columnList;

        final Table table;

        public Map<String, Column> getColumns() {
            return columns == null
                    ? columns = columnList
                    .stream()
                    .collect(Collectors.toMap(Column::getAlias, Function.identity(), (a, b) -> b))
                    : columns;
        }
    }

    @Getter
    @AllArgsConstructor
    class Table {
        final String alias;

        final TableOrViewMetadata metadata;
    }

    @AllArgsConstructor
    @Getter
    class Column {
        //列名
        String name;
        //别名
        String alias;
        //所有者
        String owner;
        //元数据信息
        RDBColumnMetadata metadata;
    }

    class SelectTable extends Table {
        final Map<String, Column> columns;

        public SelectTable(String alias,
                           Map<String, Column> columns,
                           TableOrViewMetadata metadata) {
            super(alias, metadata);
            this.columns = columns;
        }

        public Map<String, Column> getColumns() {
            return Collections.unmodifiableMap(columns);
        }
    }


}
