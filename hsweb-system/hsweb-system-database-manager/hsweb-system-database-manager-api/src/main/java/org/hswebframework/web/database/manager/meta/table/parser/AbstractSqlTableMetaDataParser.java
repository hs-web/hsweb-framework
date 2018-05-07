package org.hswebframework.web.database.manager.meta.table.parser;

import lombok.SneakyThrows;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.hswebframework.ezorm.core.ObjectWrapper;
import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.database.manager.meta.table.ColumnMetadata;
import org.hswebframework.web.database.manager.meta.table.TableMetadata;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.datasource.DatabaseType;

import java.sql.SQLException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public abstract class AbstractSqlTableMetaDataParser implements TableMetaDataParser, MetaDataParserSupplier {

    private SqlExecutor sqlExecutor;

    private static ColumnMetadataWrapper wrapper = new ColumnMetadataWrapper();

    public abstract String getSelectTableColumnsSql();

    public abstract String getSelectTableMetaSql();

    public abstract String getSelectAllTableSql();

    public AbstractSqlTableMetaDataParser(SqlExecutor sqlExecutor, DatabaseType... databaseTypes) {
        this.sqlExecutor = sqlExecutor;
        supportDataBases.addAll(Arrays.asList(databaseTypes));
    }

    private Set<DatabaseType> supportDataBases = new HashSet<>();

    @Override
    public boolean isSupport(DatabaseType type) {
        return supportDataBases.contains(type);
    }

    @Override
    public MetaDataParser get() {
        return this;
    }

    @Override
    public List<TableMetadata> parseAll() throws SQLException {
        String dsId = DataSourceHolder.switcher().currentDataSourceId();
        return sqlExecutor.list(getSelectAllTableSql())
                .parallelStream()
                .map(map -> map.get("name"))
                .map(String::valueOf)
                .map(tableName -> {
                    try {
                        DataSourceHolder.switcher().use(dsId);
                        return this.parse(tableName);
                    } finally {
                        DataSourceHolder.switcher().reset();
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @SneakyThrows
    public TableMetadata parse(String objectName) {

        Map<String, Object> param = new HashMap<>();
        param.put("table", objectName);

        Map<String, Object> tableMetaMap = sqlExecutor.single(getSelectTableMetaSql(), param);

        if (tableMetaMap == null) {
            return null;
        }
        TableMetadata table = new TableMetadata();
        table.setName(objectName);
        table.setComment((String) tableMetaMap.getOrDefault("comment", ""));
        List<ColumnMetadata> columns = sqlExecutor.list(getSelectTableColumnsSql(), param, wrapper);

        table.setColumns(columns);

        return table;
    }


    static class ColumnMetadataWrapper implements ObjectWrapper<ColumnMetadata> {
        static Map<String, BiConsumer<ColumnMetadata, Object>> propertySetters = new HashMap<>();

        static {
            propertySetters.put("name", (columnMetadata, value) -> columnMetadata.setName(String.valueOf(value)));

        }

        @Override
        public Class<ColumnMetadata> getType() {
            return ColumnMetadata.class;
        }

        @Override
        public ColumnMetadata newInstance() {
            return new ColumnMetadata();
        }

        @Override
        public void wrapper(ColumnMetadata instance, int index, String attr, Object value) {
            try {
                BeanUtilsBean.getInstance().setProperty(instance, attr, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean done(ColumnMetadata instance) {
            return true;
        }
    }
}
