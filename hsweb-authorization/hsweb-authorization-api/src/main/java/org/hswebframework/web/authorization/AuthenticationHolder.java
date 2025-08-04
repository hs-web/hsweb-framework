/*
 *  Copyright 2020 http://www.hswebframework.org
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

import io.netty.util.concurrent.FastThreadLocal;
import lombok.SneakyThrows;
import org.hswebframework.web.authorization.simple.SimpleAuthentication;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static final FastThreadLocal<Authentication> CURRENT = new FastThreadLocal<>();


    private static Optional<Authentication> get(Function<AuthenticationSupplier, Optional<Authentication>> function) {
        int size = suppliers.size();
        if (size == 0) {
            return Optional.empty();
        }
        if (size == 1) {
            return function.apply(suppliers.get(0));
        }
        ReactiveAuthenticationHolder.AuthenticationMerging merging
            = new ReactiveAuthenticationHolder.AuthenticationMerging();
        for (AuthenticationSupplier supplier : suppliers) {
            function.apply(supplier).ifPresent(merging::merge);
        }
        return Optional.ofNullable(merging.get());
    }


    /**
     * @return 当前登录的用户权限信息
     */
    public static Optional<Authentication> get() {
        Authentication current = CURRENT.getIfExists();
        if (current != null) {
            return Optional.of(current);
        }
        return get(AuthenticationSupplier::get);
    }

    /**
     * 获取指定用户的权限信息
     *
     * @param userId 用户ID
     * @return 权限信息
     */
    public static Optional<Authentication> get(String userId) {
        return get(supplier -> supplier.get(userId));
    }

    /**
     * 初始化 {@link AuthenticationSupplier}
     *
     * @param supplier 认证信息提供者
     */
    public static void addSupplier(AuthenticationSupplier supplier) {
        lock.writeLock().lock();
        try {
            suppliers.add(supplier);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void resetCurrent() {
        CURRENT.remove();
    }

    public static void makeCurrent(Authentication authentication) {
        if (authentication == null) {
            resetCurrent();
        } else {
            CURRENT.set(authentication);
        }
    }

    /**
     * 指定用户权限，执行一个任务。任务执行过程中可通过 {@link Authentication#current()}获取到当前权限.
     *
     * @param current  当前用户权限信息
     * @param callable 任务执行器
     * @param <T>      任务执行结果类型
     * @return 任务执行结果
     */
    @SneakyThrows
    public static <T> T executeWith(Authentication current, Callable<T> callable) {
        Authentication previous = CURRENT.getIfExists();
        try {
            CURRENT.set(current);
            return callable.call();
        } finally {
            CURRENT.set(previous);
        }
    }

}
