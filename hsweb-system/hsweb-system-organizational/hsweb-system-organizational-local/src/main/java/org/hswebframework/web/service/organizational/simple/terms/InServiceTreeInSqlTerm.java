package org.hswebframework.web.service.organizational.simple.terms;

import lombok.Getter;
import org.hswebframework.web.commons.entity.TreeSupportEntity;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.service.QueryService;
import org.hswebframework.web.service.terms.TreeStructureSqlTermCustomizer;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public class InServiceTreeInSqlTerm<PK> extends TreeStructureSqlTermCustomizer {

    private QueryService<? extends TreeSupportEntity<PK>, PK> treeService;

    private String tableName;

    @Getter
    private String name;

    public InServiceTreeInSqlTerm(QueryService<? extends TreeSupportEntity<PK>, PK> service,
                                  String name,
                                  String prefix,
                                  String tableName) {
        super(prefix);
        this.treeService = service;
        this.tableName = tableName;
        this.name = name;
    }

    @Override
    protected String getTableName() {
        String db = DataSourceHolder.databaseSwitcher().currentDatabase();
        if (db != null) {
            return db.concat(".").concat(tableName);
        }
        return tableName;
    }

    @Override
    protected List<String> getTreePathByTerm(List<Object> termValue) {

        List<PK> idList = ((List) termValue);

        return treeService.selectByPk(idList)
                .stream()
                .map(TreeSupportEntity::getPath)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

    }
}
