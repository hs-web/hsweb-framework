package org.hswebframework.web.system.authorization.api.service;

import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface UserService {

    boolean saveUser(UserEntity userEntity);

    Optional<UserEntity> findByUsername(@NotEmpty String username);

    Optional<UserEntity> findByUsernameAndPassword(@NotEmpty String username, @NotEmpty String plainPassword);

    Optional<UserEntity> findById(String id);

    List<UserEntity> findById(Collection<String> ids);

    boolean changeState(String userId, byte state);

    void changePassword(String userId, String oldPassword, String newPassword);

    List<UserEntity> findUser(QueryParam queryParam);

    long countUser(QueryParam queryParam);

    PagerResult<UserEntity> findUserPager(QueryParam param);
}
