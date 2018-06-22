package org.hswebframework.web.workflow.dimension.parser;

import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.service.authorization.UserService;
import org.hswebframework.web.workflow.dimension.DimensionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.hswebframework.web.commons.entity.param.QueryParamEntity.*;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Component
@ConditionalOnBean(UserService.class)
public class PositionCandidateDimensionParserStrategy implements CandidateDimensionParserStrategy {

    @Autowired(required = false)
    private UserService userService;

    @Override
    public boolean support(String dimension) {
        return DIMENSION_POSITION.equals(dimension) && userService != null;
    }

    @Override
    public List<String> parse(DimensionContext context, StrategyConfig config) {
        String type = config.getConfig("tree")
                .map(String::valueOf)
                .map("-"::concat)
                .orElse("");
        return userService.select(
                empty().noPaging()
                        //https://github.com/hs-web/hsweb-framework/tree/master/hsweb-system/hsweb-system-organizational#sql条件
                        .where("id", "user-in-position"+type, config.getIdList()))
                .stream()
                .map(UserEntity::getId)
                .collect(Collectors.toList());
    }
}
