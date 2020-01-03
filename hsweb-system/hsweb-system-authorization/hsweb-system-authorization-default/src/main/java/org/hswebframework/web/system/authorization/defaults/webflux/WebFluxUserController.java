package org.hswebframework.web.system.authorization.defaults.webflux;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.DeleteAction;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.authorization.annotation.SaveAction;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceQueryController;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.hswebframework.web.system.authorization.defaults.service.DefaultReactiveUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/user")
@Authorize
@Resource(id = "user", name = "系统用户", group = "system")
public class WebFluxUserController implements ReactiveServiceQueryController<UserEntity, String> {

    @Autowired
    private DefaultReactiveUserService reactiveUserService;

    @PatchMapping
    @SaveAction
    public Mono<Boolean> saveUser(@RequestBody Mono<UserEntity> user) {
        return Authentication
                .currentReactive()
                .zipWith(user, ((u, e) -> {
                    e.setCreateTimeNow();
                    e.setCreatorId(u.getUser().getId());
                    return e;
                }))
                .switchIfEmpty(user)
                .as(reactiveUserService::saveUser);
    }

    @PutMapping("/{id:.+}/{state}")
    @SaveAction
    public Mono<Integer> changeState(@PathVariable String id, @PathVariable Byte state) {
        return reactiveUserService.changeState(Mono.just(id), state);
    }

    @DeleteMapping("/{id:.+}")
    @DeleteAction
    public Mono<Boolean> deleteUser(@PathVariable String id) {
        return reactiveUserService.deleteUser(id);
    }

    @PutMapping("/passwd")
    @Authorize(merge = false)
    public Mono<Boolean> changePassword(@RequestBody ChangePasswordRequest request) {
        return Authentication
                .currentReactive()
                .switchIfEmpty(Mono.error(UnAuthorizedException::new))
                .map(Authentication::getUser)
                .map(User::getId)
                .flatMap(userId -> reactiveUserService.changePassword(userId, request.getOldPassword(), request.getNewPassword()));
    }

    @PutMapping("/me")
    @Authorize(merge = false)
    public Mono<Boolean> updateLoginUserInfo(@RequestBody UserEntity request) {
        return Authentication
                .currentReactive()
                .switchIfEmpty(Mono.error(UnAuthorizedException::new))
                .map(Authentication::getUser)
                .map(User::getId)
                .flatMap(userId -> reactiveUserService.updateById(userId, Mono.just(request)).map(integer -> integer > 0));
    }

    @Override
    public DefaultReactiveUserService getService() {
        return reactiveUserService;
    }

    @Getter
    @Setter
    public static class ChangePasswordRequest {
        private String oldPassword;

        private String newPassword;
    }
}
