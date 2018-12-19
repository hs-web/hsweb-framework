package org.hswebframework.web.starter.init;

import lombok.AllArgsConstructor;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.meta.RDBTableMetaData;
import org.hswebframework.ezorm.rdb.meta.builder.ColumnBuilder;
import org.hswebframework.ezorm.rdb.meta.builder.IndexBuilder;
import org.hswebframework.ezorm.rdb.meta.builder.TableBuilder;

import java.sql.JDBCType;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class DoNotingTableBuilder implements TableBuilder {

    @AllArgsConstructor
    public static class DoNotionColumnBuilder implements ColumnBuilder {
        private TableBuilder tableBuilder;

        @Override
        public ColumnBuilder custom(Consumer<RDBColumnMetaData> consumer) {
            return this;
        }

        @Override
        public ColumnBuilder name(String s) {
            return this;
        }

        @Override
        public ColumnBuilder alias(String s) {
            return this;
        }

        @Override
        public ColumnBuilder dataType(String s) {
            return this;
        }

        @Override
        public ColumnBuilder jdbcType(JDBCType jdbcType) {
            return this;
        }

        @Override
        public ColumnBuilder javaType(Class aClass) {
            return this;
        }

        @Override
        public ColumnBuilder comment(String s) {
            return this;
        }

        @Override
        public ColumnBuilder notNull() {
            return this;
        }

        @Override
        public ColumnBuilder primaryKey() {
            return this;
        }

        @Override
        public ColumnBuilder columnDef(String s) {
            return this;
        }

        @Override
        public ColumnBuilder property(String s, Object o) {
            return this;
        }

        @Override
        public ColumnBuilder length(int i) {
            return this;
        }

        @Override
        public ColumnBuilder length(int i, int i1) {
            return this;
        }

        @Override
        public TableBuilder commit() {
            return tableBuilder;
        }
    }

    @Override
    public TableBuilder addColumn(Set<RDBColumnMetaData> set) {
        return this;
    }

    @Override
    public TableBuilder custom(Consumer<RDBTableMetaData> consumer) {
        return this;
    }

    @Override
    public ColumnBuilder addColumn() {
        return new DoNotionColumnBuilder(this);
    }

    @Override
    public ColumnBuilder addOrAlterColumn(String s) {
        return new DoNotionColumnBuilder(this);
    }

    @Override
    public TableBuilder removeColumn(String s) {
        return this;
    }

    @Override
    public TableBuilder comment(String s) {
        return this;
    }

    @Override
    public TableBuilder property(String s, Object o) {
        return this;
    }

    @Override
    public TableBuilder alias(String s) {
        return this;
    }

    @Override
    public IndexBuilder index() {
        TableBuilder builder = this;
        return new IndexBuilder(this, null) {
            @Override
            public TableBuilder commit() {
                return builder;
            }
        };
    }

    @Override
    public void commit() {

    }
}
