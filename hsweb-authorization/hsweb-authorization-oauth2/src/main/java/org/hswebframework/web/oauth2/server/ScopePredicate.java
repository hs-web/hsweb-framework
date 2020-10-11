package org.hswebframework.web.oauth2.server;

import java.util.function.BiPredicate;

@FunctionalInterface
public interface ScopePredicate extends BiPredicate<String, String[]> {

    boolean test(String permission, String... actions);

}
