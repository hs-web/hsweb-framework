package org.hswebframework.web.service.organizational.simple.terms;

import org.hswebframework.web.commons.entity.TreeSupportEntity;
import org.hswebframework.web.dao.mybatis.mapper.TreeStructureSqlTermCustomizer;
import org.hswebframework.web.service.QueryService;

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

    public InServiceTreeInSqlTerm(QueryService<? extends TreeSupportEntity<PK>, PK> service,
                                  String prefix,
                                  String tableName,
                                  boolean not, boolean parent) {
        super(prefix + "-" + (parent ? "parent" : "child") + "-" + (not ? "not-" : "") + "in", not, parent);
        this.treeService = service;
        this.tableName = tableName;
    }

    @Override
    protected String getTableName() {
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
