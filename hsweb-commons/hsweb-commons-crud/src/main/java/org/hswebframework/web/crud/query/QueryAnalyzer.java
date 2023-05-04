package org.hswebframework.web.crud.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.TableOrViewMetadata;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public interface QueryAnalyzer {

    String nativeSql();

    SqlRequest refactor(QueryParamEntity entity, Object... args);

    SqlRequest refactorCount(QueryParamEntity entity, Object... args);


    Select select();

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
        String name;
        String alias;
        String owner;
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
    }


}
