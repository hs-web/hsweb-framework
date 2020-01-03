package org.hswebframework.web.system.authorization.api.service.reactive;

import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface ReactiveUserService {

    Mono<UserEntity> newUserInstance();

    Mono<Boolean> saveUser(Mono<UserEntity> userEntity);

    Mono<UserEntity> findByUsername(String username);

    Mono<UserEntity> findById(String id);

    Mono<UserEntity> findByUsernameAndPassword(String username, String plainPassword);

    Mono<Integer> changeState(Publisher<String> userId, byte state);

    Mono<Boolean> changePassword(String userId, String oldPassword, String newPassword);

    Flux<UserEntity> findUser(QueryParam queryParam);

    Mono<Integer> countUser(QueryParam queryParam);

    Mono<Boolean> deleteUser(String userId);

}
