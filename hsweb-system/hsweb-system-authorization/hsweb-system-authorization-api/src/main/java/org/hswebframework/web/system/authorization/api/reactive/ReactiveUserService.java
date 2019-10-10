package org.hswebframework.web.system.authorization.api.reactive;

import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.web.system.authorization.api.User;
import org.hswebframework.web.system.authorization.api.request.SaveUserRequest;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface ReactiveUserService {

    Flux<User> save(Publisher<SaveUserRequest> userEntity);

    Mono<User> getByUsername(String username);

    Mono<User> getByUsernameAndPassword(String username, String plainPassword);

    Mono<Integer> changeState(Publisher<String> userId, byte state);

    Mono<Boolean> updatePassword(String userId, String oldPassword, String newPassword);

    Flux<User> findUser(QueryParam queryParam);

    Mono<Long> countUser(QueryParam queryParam);

}
