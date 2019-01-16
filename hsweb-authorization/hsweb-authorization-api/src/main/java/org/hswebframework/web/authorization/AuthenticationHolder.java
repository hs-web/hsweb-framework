/*
 *  Copyright 2019 http://www.hswebframework.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.hswebframework.web.authorization;

import org.hswebframework.web.ThreadLocalUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * 权限获取器,用于静态方式获取当前登录用户的权限信息.
 * 例如:
 * <pre>
 *     &#064;RequestMapping("/example")
 *     public ResponseMessage example(){
 *         Authorization auth = AuthorizationHolder.get();
 *         return ResponseMessage.ok();
 *     }
 * </pre>
 *
 * @author zhouhao
 * @see AuthenticationSupplier
 * @since 3.0
 */
public final class AuthenticationHolder {
    private static final List<AuthenticationSupplier> suppliers = new ArrayList<>();

    private static final String CURRENT_USER_ID_KEY = Authentication.class.getName() + "_current_id";

    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static Authentication get(Function<AuthenticationSupplier, Authentication> function) {
        lock.readLock().lock();
        try {
            return suppliers.stream()
                    .map(function)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @return 当前登录的用户权限信息
     */
    public static Authentication get() {
        String currentId = ThreadLocalUtils.get(CURRENT_USER_ID_KEY);
        if (currentId != null) {
            return get(currentId);
        }
        return get(AuthenticationSupplier::get);
    }

    /**
     * 获取指定用户的权限信息
     *
     * @param userId 用户ID
     * @return 权限信息
     */
    public static Authentication get(String userId) {
        return get(supplier -> supplier.get(userId));
    }

    /**
     * 初始化 {@link AuthenticationSupplier}
     *
     * @param supplier
     */
    public static void addSupplier(AuthenticationSupplier supplier) {
        lock.writeLock().lock();
        try {
            suppliers.add(supplier);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void setCurrentUserId(String id) {
        ThreadLocalUtils.put(AuthenticationHolder.CURRENT_USER_ID_KEY, id);
    }
}
