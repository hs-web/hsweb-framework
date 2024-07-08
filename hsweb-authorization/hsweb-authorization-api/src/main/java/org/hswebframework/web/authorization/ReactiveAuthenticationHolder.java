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

import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.web.authorization.simple.SimpleAuthentication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 响应式权限保持器,用于响应式方式获取当前登录用户的权限信息.
 * 例如:
 * <pre>{@code
 *     @RequestMapping("/example")
 *     public Mono<Authorization> example(){
 *         return ReactiveAuthenticationHolder.get();
 *     }
 *     }
 * </pre>
 *
 * @author zhouhao
 * @see ReactiveAuthenticationSupplier
 * @since 4.0
 */
public final class ReactiveAuthenticationHolder {
    private static final List<ReactiveAuthenticationSupplier> suppliers = new CopyOnWriteArrayList<>();

    private static Mono<Authentication> get(Function<ReactiveAuthenticationSupplier, Mono<Authentication>> function) {

        return Flux
                .merge(suppliers
                               .stream()
                               .map(function)
                               .collect(Collectors.toList()))
                .collectList()
                .filter(CollectionUtils::isNotEmpty)
                .map(all -> {
                    if (all.size() == 1) {
                        return all.get(0);
                    }
                    SimpleAuthentication authentication = new SimpleAuthentication();
                    for (Authentication auth : all) {
                        authentication.merge(auth);
                    }
                    return authentication;
                });
    }

    /**
     * @return 当前登录的用户权限信息
     */
    public static Mono<Authentication> get() {

        return get(ReactiveAuthenticationSupplier::get);
    }

    /**
     * 获取指定用户的权限信息
     *
     * @param userId 用户ID
     * @return 权限信息
     */
    public static Mono<Authentication> get(String userId) {
        return get(supplier -> supplier.get(userId));
    }

    /**
     * 初始化 {@link ReactiveAuthenticationSupplier}
     *
     * @param supplier
     */
    public static void addSupplier(ReactiveAuthenticationSupplier supplier) {
        suppliers.add(supplier);
    }

    public static void setSupplier(ReactiveAuthenticationSupplier supplier) {
        suppliers.clear();
        suppliers.add(supplier);
    }

}
