package org.hswebframework.web.service.organizational.simple.terms;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.PrepareSqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.function.FunctionFragmentBuilder;
import org.hswebframework.web.commons.entity.TreeSupportEntity;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.service.QueryService;
import org.hswebframework.web.service.terms.AbstractSqlTermCustomizer;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 查询根据用户查询某🀄️数据
 *
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Slf4j
public abstract class UserInSqlTerm<PK> extends AbstractSqlTermCustomizer {



    @Getter
    @Setter
    private boolean forPerson;

    protected QueryService<? extends TreeSupportEntity<PK>, PK> treeService;



    public UserInSqlTerm<PK> forPerson() {
        this.forPerson = true;
        return this;
    }

    public UserInSqlTerm(String term, QueryService<? extends TreeSupportEntity<PK>, PK> treeService) {
        super(term);
        this.treeService = treeService;
    }

    public abstract String getTableName();

    protected String getTableFullName(String tableName) {
        String db = DataSourceHolder.databaseSwitcher().currentDatabase();
        if (db != null) {
            return db.concat(".").concat(tableName);
        }
        return tableName;
    }

    @SuppressWarnings("all")
    protected void appendCondition(String table, PrepareSqlFragments fragments, RDBColumnMetadata column, Term term, List<Object> values) {
        boolean not = term.getOptions().contains("not");

        boolean child = term.getOptions().contains("child");
        boolean parent = term.getOptions().contains("parent");

        if (!child && !parent) {
            super.appendCondition(fragments, column, values);
        } else {
            List<String> paths = getTreePathByTerm(values)
                    .stream()
                    .map(path -> parent ? path : path.concat("%"))
                    .collect(Collectors.toList());

            int len = paths.size();
            if (len == 0) {
                fragments.addSql("1=2");
            } else {
                fragments.addSql("(");
                for (int i = 0; i < len; i++) {
                    if (i > 0) {
                        fragments.addSql("or");
                    }
                    if (parent) {
                        FunctionFragmentBuilder function = column.findFeature(FunctionFragmentBuilder.createFeatureId("concat")).orElse(null);

                        String concat;
                        if (function == null) {
                            concat = getTableName() + ".path";
                            log.warn("数据库不支持concat函数(FunctionFragmentBuilder)!");
                            fragments.addSql("? like ", paths.get(i)).addSql("like", concat);
                        } else {
                            Map<String, Object> param = new HashMap<>();
                            param.put("0", "'tmp.path'");
                            param.put("1", "'%'");

                            //? like concat(tmp.path,'%')
                            fragments.addSql("? like ", paths.get(i)).addSql("like").addFragments(function.create(table, column, param));
                        }

                    } else {
                        fragments.addSql(table, "like ?", paths.get(i));
                    }

                }
                fragments.addSql(")");
            }
        }
    }


    protected List<String> getTreePathByTerm(List<Object> termValue) {

        List<PK> idList = ((List) termValue);

        return treeService.selectByPk(idList)
                .stream()
                .map(TreeSupportEntity::getPath)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

    }
}
