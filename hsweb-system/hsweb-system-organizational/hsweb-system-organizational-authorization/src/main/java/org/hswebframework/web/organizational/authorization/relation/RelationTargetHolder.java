package org.hswebframework.web.organizational.authorization.relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author zhouhao
 * @since 3.0
 */
public final class RelationTargetHolder {
    private static List<RelationTargetSupplier> suppliers = new ArrayList<>(16);

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> Optional<T> get(String type, String target) {
        return (Optional<T>) suppliers.stream()
                .filter(supplier -> supplier.support(type, target))
                .map(supplier -> supplier.get(target))
                .findFirst();
    }

    static void addSupplier(RelationTargetSupplier supplier) {
        suppliers.add(supplier);
    }
}
