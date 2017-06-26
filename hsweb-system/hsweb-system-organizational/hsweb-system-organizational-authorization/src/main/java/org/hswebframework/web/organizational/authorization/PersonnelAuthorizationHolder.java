package org.hswebframework.web.organizational.authorization;

import org.hswebframework.web.ThreadLocalUtils;
import org.hswebframework.web.authorization.AuthenticationSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class PersonnelAuthorizationHolder {
    private static final List<PersonnelAuthorizationSupplier> suppliers = new ArrayList<>();

    private static final String CURRENT_USER_ID_KEY = PersonnelAuthorizationHolder.class.getName() + "_current_id";

    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static PersonnelAuthorization get(Function<PersonnelAuthorizationSupplier, PersonnelAuthorization> function) {
        lock.readLock().lock();
        try {
            return suppliers.stream()
                    .map(function)
                    .filter(Objects::nonNull)
                    .findFirst().orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @return 当前登录的用户权限信息
     */
    public static PersonnelAuthorization get() {
        String currentId = ThreadLocalUtils.get(CURRENT_USER_ID_KEY);
        if (currentId != null) {
            return getByPersonId(currentId);
        }
        return get(PersonnelAuthorizationSupplier::get);
    }

    public static PersonnelAuthorization getByUserId(String userId) {
        return get(supplier -> supplier.getByUserId(userId));
    }

    public static PersonnelAuthorization getByPersonId(String personId) {
        return get(supplier -> supplier.getByPersonId(personId));
    }

    /**
     * 初始化 {@link AuthenticationSupplier}
     *
     * @param supplier
     */
    public static void addSupplier(PersonnelAuthorizationSupplier supplier) {
        lock.writeLock().lock();
        try {
            suppliers.add(supplier);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void setCurrentPersonId(String id) {
        ThreadLocalUtils.put(PersonnelAuthorizationHolder.CURRENT_USER_ID_KEY, id);
    }

}
