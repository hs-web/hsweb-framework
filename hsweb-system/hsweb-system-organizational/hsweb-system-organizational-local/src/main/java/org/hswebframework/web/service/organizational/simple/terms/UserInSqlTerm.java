package org.hswebframework.web.service.organizational.simple.terms;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.dialect.term.BoostTermTypeMapper;
import org.hswebframework.web.commons.entity.TreeSupportEntity;
import org.hswebframework.web.dao.mybatis.mapper.AbstractSqlTermCustomer;
import org.hswebframework.web.service.QueryService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 查询岗位中的用户
 *
 * @author zhouhao
 * @since 3.0.0-RC
 */
public abstract class UserInSqlTerm<PK> extends AbstractSqlTermCustomer {


    @Setter
    @Getter
    private boolean child;
    @Getter
    @Setter
    private boolean forPerson;

    QueryService<? extends TreeSupportEntity<PK>, PK> treeService;


    public UserInSqlTerm<PK> forPerson() {
        this.forPerson = true;
        return this;
    }

    public UserInSqlTerm(String term, QueryService<? extends TreeSupportEntity<PK>, PK> treeService) {
        super(term);
        this.treeService = treeService;
    }

    public abstract String getTableName();

    protected Object appendCondition(List<Object> values, String wherePrefix, SqlAppender appender, String column) {
        if (!child) {
            appender.addSpc(column);
            return super.appendCondition(values, wherePrefix, appender);
        } else {
            List<String> paths = getTreePathByTerm(values)
                    .stream()
                    .map(path -> path.concat("%"))
                    .collect(Collectors.toList());
            int len = paths.size();
            if (len == 0) {
                appender.add("1=2");
            } else {
                appender.add("(");
                for (int i = 0; i < len; i++) {
                    if (i > 0) {
                        appender.addSpc("or");
                    }
                    appender.add(getTableName() + ".path like #{", wherePrefix, ".value.value[", i, "]}");
                }
                appender.add(")");
            }
            return paths;
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
