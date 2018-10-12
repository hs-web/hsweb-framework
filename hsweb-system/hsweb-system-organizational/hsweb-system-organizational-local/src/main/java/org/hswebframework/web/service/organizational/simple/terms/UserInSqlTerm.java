package org.hswebframework.web.service.organizational.simple.terms;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.dialect.Dialect;
import org.hswebframework.ezorm.rdb.render.dialect.RenderPhase;
import org.hswebframework.ezorm.rdb.render.dialect.function.SqlFunction;
import org.hswebframework.web.commons.entity.TreeSupportEntity;
import org.hswebframework.web.dao.mybatis.mapper.AbstractSqlTermCustomizer;
import org.hswebframework.web.service.QueryService;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 查询岗位中的用户
 *
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Slf4j
public abstract class UserInSqlTerm<PK> extends AbstractSqlTermCustomizer {


    @Setter
    @Getter
    private boolean child;

    @Getter
    @Setter
    private boolean parent;

    @Getter
    @Setter
    private boolean forPerson;

    QueryService<? extends TreeSupportEntity<PK>, PK> treeService;


    public UserInSqlTerm<PK> forChild() {
        setChild(true);
        return this;
    }

    public UserInSqlTerm<PK> forParent() {
       setParent(true);
        return this;
    }

    public UserInSqlTerm<PK> forPerson() {
        this.forPerson = true;
        return this;
    }

    public UserInSqlTerm(String term, QueryService<? extends TreeSupportEntity<PK>, PK> treeService) {
        super(term);
        this.treeService = treeService;
    }

    public abstract String getTableName();


    protected Object appendCondition(List<Object> values, String wherePrefix, SqlAppender appender, String column, Dialect dialect) {
        if (!child&&!parent) {
            appender.addSpc(column);
            return super.appendCondition(values, wherePrefix, appender);
        } else {
            List<String> paths = getTreePathByTerm(values)
                    .stream()
                    .map(path -> parent ? path : path.concat("%"))
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
                    if (parent) {
                        SqlFunction function = dialect.getFunction(SqlFunction.concat);
                        String concat;
                        if (function == null) {
                            concat = getTableName() + ".path";
                            log.warn("数据库方言未支持concat函数,你可以调用Dialect.installFunction进行设置!");
                        } else {
                            concat = function.apply(SqlFunction.Param.of(RenderPhase.where, Arrays.asList(getTableName() + ".path", "'%'")));
                        }
                        // aaa-vvv-ccc like aaa%
                        appender.add("#{", wherePrefix, ".value.value[", i, "]}", " like ", concat);
                    } else {
                        appender.add(getTableName(), ".path like #{", wherePrefix, ".value.value[", i, "]}");
                    }

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
