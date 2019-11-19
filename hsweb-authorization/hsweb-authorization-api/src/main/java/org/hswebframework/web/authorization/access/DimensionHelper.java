package org.hswebframework.web.authorization.access;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.simple.DimensionDataAccessConfig;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class DimensionHelper {


    public static Set<Object> getDimensionDataAccessScope(Authentication atz,
                                                          Permission permission,
                                                          String action,
                                                          String dimensionType) {
        return permission
                .getDataAccesses(action)
                .stream()
                .filter(DimensionDataAccessConfig.class::isInstance)
                .map(DimensionDataAccessConfig.class::cast)
                .filter(conf -> dimensionType.equals(conf.getScopeType()))
                .flatMap(conf -> {
                    if (CollectionUtils.isEmpty(conf.getScope())) {
                        return atz.getDimensions(dimensionType)
                                .stream()
                                .map(Dimension::getId);
                    }
                    return conf.getScope().stream();
                }).collect(Collectors.toSet());
    }

    public static Set<Object> getDimensionDataAccessScope(Authentication atz,
                                                          Permission permission,
                                                          String action,
                                                          DimensionType dimensionType) {
        return getDimensionDataAccessScope(atz, permission, action, dimensionType.getId());
    }


    public static Set<Object> getDimensionDataAccessScope(Authentication atz,
                                                          String permission,
                                                          String action,
                                                          String dimensionType) {
        return atz
                .getPermission(permission)
                .map(per -> getDimensionDataAccessScope(atz, per, action, dimensionType)).orElseGet(Collections::emptySet);
    }

    public static Set<Object> getDimensionDataAccessScope(Authentication atz,
                                                          String permission,
                                                          String action,
                                                          DimensionType dimensionType) {
        return atz
                .getPermission(permission)
                .map(per -> getDimensionDataAccessScope(atz, per, action, dimensionType))
                .orElseGet(Collections::emptySet);
    }

}
