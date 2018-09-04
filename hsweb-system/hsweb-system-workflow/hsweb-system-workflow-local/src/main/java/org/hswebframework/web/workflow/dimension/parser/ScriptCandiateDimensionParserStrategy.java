package org.hswebframework.web.workflow.dimension.parser;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.expands.script.engine.DynamicScriptEngine;
import org.hswebframework.expands.script.engine.DynamicScriptEngineFactory;
import org.hswebframework.web.Lists;
import org.hswebframework.web.Maps;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.entity.organizational.PersonEntity;
import org.hswebframework.web.organizational.authorization.relation.PersonRelations;
import org.hswebframework.web.organizational.authorization.relation.Relation;
import org.hswebframework.web.organizational.authorization.relation.RelationsManager;
import org.hswebframework.web.workflow.dimension.DimensionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Slf4j
@ConditionalOnBean(RelationsManager.class)
public class ScriptCandiateDimensionParserStrategy implements CandidateDimensionParserStrategy {

    @Autowired
    private RelationsManager relationsManager;

    @Override
    public boolean support(String dimension) {
        return DIMENSION_RELATION.equals(dimension);
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("all")
    public List<String> parse(DimensionContext context, StrategyConfig config) {
        String expression = config.getStringConfig("expression").orElse(null);
        String expressionLanguage = config.getStringConfig("expressionLanguage").orElse(null);

        if (StringUtils.isEmpty(expression)) {
            return new java.util.ArrayList<>();
        }

        String creatorId = context.getCreatorId();

        Supplier<PersonRelations> creator = () -> relationsManager.getPersonRelationsByUserId(creatorId);

        DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(expressionLanguage);

        if (engine == null) {
            throw new UnsupportedOperationException("不支持的脚本:" + expressionLanguage);
        }

        String id = DigestUtils.md5DigestAsHex(expression.getBytes());
        if (!engine.compiled(id)) {
            engine.compile(id, expression);
        }

        Object obj = engine.execute(id, Maps.<String, Object>buildMap()
                .put("user", creator)
                .put("creator", creator)
                .put("creatorId", creatorId)
                .put("context", context)
                .get())
                .getIfSuccess();

        Function<Object, String> userIdConverter = o -> {
            if (o instanceof String) {
                return (String) o;
            } else if (o instanceof Relation) {
                Object target = ((Relation) o).getTargetObject();
                if (target instanceof PersonEntity) {
                    return ((PersonEntity) target).getUserId();
                } else if (target instanceof UserEntity) {
                    return ((UserEntity) target).getId();
                } else {
                    return ((Relation) o).getTarget();
                }
            } else {
                log.warn("不支持的关系:{}", o);
                return null;
            }
        };

        if (obj instanceof List) {
            List<Object> list = ((List) obj);
            return list.stream()
                    .map(userIdConverter)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            String result = userIdConverter.apply(obj);
            if (result == null) {
                return new java.util.ArrayList<>();
            }
            return Lists.buildList(result).get();
        }
    }

}
