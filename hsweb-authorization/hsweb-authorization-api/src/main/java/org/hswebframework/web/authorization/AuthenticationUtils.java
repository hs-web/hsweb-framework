package org.hswebframework.web.authorization;

import org.springframework.util.StringUtils;

/**
 * @author zhouhao
 * @since 3.0
 */
public class AuthenticationUtils {

    public static AuthenticationPredicate createPredicate(String expression) {
        if (StringUtils.isEmpty(expression)) {
            return (authentication -> false);
        }
        AuthenticationPredicate main = null;
        // resource:user:add or update
        AuthenticationPredicate temp = null;
        boolean lastAnd = true;
        for (String conf : expression.split("[ ]")) {
            if (conf.startsWith("resource:")||conf.startsWith("permission:")) {
                String[] permissionAndActions = conf.split("[:]", 2);
                if (permissionAndActions.length < 2) {
                    temp = authentication -> !authentication.getPermissions().isEmpty();
                } else {
                    String[] real = permissionAndActions[1].split("[:]");
                    temp = real.length > 1 ?
                            AuthenticationPredicate.permission(real[0], real[1].split("[,]"))
                            : AuthenticationPredicate.permission(real[0]);
                }
            } else if (main != null && conf.equalsIgnoreCase("and")) {
                lastAnd = true;
                main = main.and(temp);
            } else if (main != null && conf.equalsIgnoreCase("or")) {
                main = main.or(temp);
                lastAnd = false;
            } else {
                String[] real = conf.split("[:]", 2);
                if (real.length < 2) {
                    temp = AuthenticationPredicate.dimension(real[0]);
                } else {
                    temp = AuthenticationPredicate.dimension(real[0], real[1].split(","));
                }
            }
            if (main == null) {
                main = temp;
            }
        }
        return main == null ? a -> false : (lastAnd ? main.and(temp) : main.or(temp));
    }
}
