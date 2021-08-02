package org.hswebframework.web.system.authorization.defaults.webflux;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.authorization.annotation.*;
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
@Tag(name = "用户管理")
public class WebFluxUserController implements ReactiveServiceQueryController<UserEntity, String> {

    @Autowired
    private DefaultReactiveUserService reactiveUserService;

    @PatchMapping
    @SaveAction
    @Operation(summary = "保存用户信息")
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


    @PutMapping("/me")
    @Operation(summary = "修改当前用户信息")
    @ResourceAction(id = "update-self-info",name = "修改当前用户信息")
    public Mono<Boolean> updateLoginUserInfo(@RequestBody UserEntity request) {
        return Authentication
                .currentReactive()
                .switchIfEmpty(Mono.error(UnAuthorizedException::new))
                .map(Authentication::getUser)
                .map(User::getId)
                .flatMap(userId -> reactiveUserService.updateById(userId, Mono.just(request)).map(integer -> integer > 0));
    }

    @PutMapping("/{id:.+}/{state}")
    @SaveAction
    @Operation(summary = "修改用户状态")
    public Mono<Integer> changeState(@PathVariable @Parameter(description = "用户ID") String id,
                                     @PathVariable @Parameter(description = "状态,0禁用,1启用") Byte state) {
        return reactiveUserService.changeState(Mono.just(id), state);
    }

    @DeleteMapping("/{id:.+}")
    @DeleteAction
    @Operation(summary = "删除用户")
    public Mono<Boolean> deleteUser(@PathVariable String id) {
        return reactiveUserService.deleteUser(id);
    }

    @PutMapping("/passwd")
    @ResourceAction(id = "update-self-pwd",name = "修改当前用户密码")
    @Operation(summary = "修改当前用户密码")
    public Mono<Boolean> changePassword(@RequestBody ChangePasswordRequest request) {
        return Authentication
                .currentReactive()
                .switchIfEmpty(Mono.error(UnAuthorizedException::new))
                .map(Authentication::getUser)
                .map(User::getId)
                .flatMap(userId -> reactiveUserService.changePassword(userId, request.getOldPassword(), request.getNewPassword()));
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
