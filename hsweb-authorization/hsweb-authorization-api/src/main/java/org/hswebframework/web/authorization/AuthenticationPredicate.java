package org.hswebframework.web.authorization;

import org.hswebframework.web.authorization.exception.AccessDenyException;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author zhouhao
 * @since 3.0
 */
@FunctionalInterface
public interface AuthenticationPredicate extends Predicate<Authentication> {

    static AuthenticationPredicate has(String permissionString) {
        return AuthenticationUtils.createPredicate(permissionString);
    }

    static AuthenticationPredicate dimension(String dimension, String... id) {
        return autz -> autz.hasDimension(dimension, Arrays.asList(id));
    }

    static AuthenticationPredicate permission(String permissionId, String... actions) {
        return autz -> autz.hasPermission(permissionId, actions);
    }

    default AuthenticationPredicate and(String permissionString) {
        return and(has(permissionString));
    }

    default AuthenticationPredicate or(String permissionString) {
        return or(has(permissionString));
    }

    @Override
    default AuthenticationPredicate and(Predicate<? super Authentication> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) && other.test(t);
    }

    @Override
    default AuthenticationPredicate or(Predicate<? super Authentication> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) || other.test(t);
    }


    default void assertHas(Authentication authentication) {
        if (!test(authentication)) {
            throw new AccessDenyException();
        }
    }

}
