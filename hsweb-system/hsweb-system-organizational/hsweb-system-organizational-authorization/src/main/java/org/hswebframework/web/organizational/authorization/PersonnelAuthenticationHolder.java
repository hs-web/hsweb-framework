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
 * @author zhouhao
 */
public class PersonnelAuthenticationHolder {
    private static final List<PersonnelAuthenticationSupplier> suppliers = new ArrayList<>();

    private static final String CURRENT_USER_ID_KEY = PersonnelAuthenticationHolder.class.getName() + "_current_id";

    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static PersonnelAuthentication get(Function<PersonnelAuthenticationSupplier, PersonnelAuthentication> function) {
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
    public static PersonnelAuthentication get() {
        String currentId = ThreadLocalUtils.get(CURRENT_USER_ID_KEY);
        if (currentId != null) {
            return getByPersonId(currentId);
        }
        return get(PersonnelAuthenticationSupplier::get);
    }

    public static PersonnelAuthentication getByUserId(String userId) {
        return get(supplier -> supplier.getByUserId(userId));
    }

    public static PersonnelAuthentication getByPersonId(String personId) {
        return get(supplier -> supplier.getByPersonId(personId));
    }

    /**
     * 初始化 {@link AuthenticationSupplier}
     *
     * @param supplier
     */
    public static void addSupplier(PersonnelAuthenticationSupplier supplier) {
        lock.writeLock().lock();
        try {
            suppliers.add(supplier);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void setCurrentPersonId(String id) {
        ThreadLocalUtils.put(PersonnelAuthenticationHolder.CURRENT_USER_ID_KEY, id);
    }

}
