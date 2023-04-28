package org.hswebframework.web.crud.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.TableOrViewMetadata;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;

import java.util.List;
import java.util.Map;

public interface QueryAnalyzer {

    String nativeSql();

    SqlRequest inject(QueryParamEntity entity,Object... args);

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

    @AllArgsConstructor
    @Getter
    class Select {
        final Map<String, Column> columns;
        final Table table;

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
