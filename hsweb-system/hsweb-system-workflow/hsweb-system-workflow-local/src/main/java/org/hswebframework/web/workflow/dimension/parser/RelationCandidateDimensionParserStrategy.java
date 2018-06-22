package org.hswebframework.web.workflow.dimension.parser;

import org.hswebframework.web.organizational.authorization.PersonnelAuthentication;
import org.hswebframework.web.organizational.authorization.PersonnelAuthenticationManager;
import org.hswebframework.web.organizational.authorization.relation.Relations;
import org.hswebframework.web.workflow.dimension.DimensionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public class RelationCandidateDimensionParserStrategy implements CandidateDimensionParserStrategy {

    @Autowired
    PersonnelAuthenticationManager authenticationManager;


    @Override
    public boolean support(String dimension) {
        return DIMENSION_RELATION.equals(dimension);
    }

    @Override
    public List<String> parse(DimensionContext context, StrategyConfig config) {
        String userType = config.getStringConfig("userType")
                .orElse("creator");

        String user;
        switch (userType) {
            case "fixed":
                user = config.getStringConfig("userId")
                        .orElse(context.getCreatorId());
                break;
            case "pre":
                user = context.getPreTask().getOwner();
                break;
            default:
                user = context.getCreatorId();
        }

        PersonnelAuthentication authentication = authenticationManager
                .getPersonnelAuthorizationByUserId(user);

        if (null == authentication) {
            return Collections.emptyList();
        }
        Relations relations = authentication.getRelations();



        return Collections.emptyList();
    }

    public static class RelationInfo{

    }
}
