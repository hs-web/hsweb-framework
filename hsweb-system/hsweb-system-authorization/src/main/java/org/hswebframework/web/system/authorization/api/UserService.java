package org.hswebframework.web.system.authorization.api;

import org.hswebframework.web.system.authorization.entity.UserEntity;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<Boolean> save(Publisher<UserEntity> userEntity);

    Mono<UserEntity> getByUsername(String username);

    Mono<UserEntity> getByUsernameAndPassword(String username,String plainPassword);

    Mono<Boolean> changeState(Publisher<String> userId, byte state);

    Mono<Boolean> updatePassword(String userId, String oldPassword, String newPassword);

}
