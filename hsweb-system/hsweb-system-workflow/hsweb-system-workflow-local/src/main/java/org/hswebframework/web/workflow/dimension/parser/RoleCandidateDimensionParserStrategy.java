package org.hswebframework.web.workflow.dimension.parser;

import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.service.authorization.UserService;
import org.hswebframework.web.workflow.dimension.DimensionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Component
@ConditionalOnBean(UserService.class)
public class RoleCandidateDimensionParserStrategy implements CandidateDimensionParserStrategy {


    @Autowired(required = false)
    private UserService userService;


    @Override
    public boolean support(String dimension) {
        return DIMENSION_ROLE.equals(dimension) && userService != null;
    }

    @Override
    public List<String> parse(DimensionContext context, StrategyConfig config) {
        return userService.selectByUserByRole(config.getIdList())
                .stream()
                .map(UserEntity::getId)
                .collect(Collectors.toList());
    }
}
