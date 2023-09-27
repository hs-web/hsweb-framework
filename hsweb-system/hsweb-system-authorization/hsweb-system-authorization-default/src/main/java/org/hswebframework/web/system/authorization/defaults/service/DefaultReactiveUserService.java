package org.hswebframework.web.system.authorization.defaults.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.rdb.exception.DuplicateKeyException;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.api.crud.entity.TransactionManagers;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.exception.NotFoundException;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.system.authorization.api.PasswordEncoder;
import org.hswebframework.web.system.authorization.api.PasswordValidator;
import org.hswebframework.web.system.authorization.api.UsernameValidator;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.hswebframework.web.system.authorization.api.event.*;
import org.hswebframework.web.system.authorization.api.service.reactive.ReactiveUserService;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ValidationException;
import java.util.Objects;
import java.util.function.Function;


public class DefaultReactiveUserService extends GenericReactiveCrudService<UserEntity, String> implements ReactiveUserService {

    @Autowired
    private ReactiveRepository<UserEntity, String> repository;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder = (password, salt) -> DigestUtils.md5Hex(String.format("hsweb.%s.framework.%s", password, salt));

    @Autowired(required = false)
    private PasswordValidator passwordValidator = (password) -> {
    };

    @Autowired(required = false)
    private UsernameValidator usernameValidator = (username) -> {

    };

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public Mono<UserEntity> newUserInstance() {
        return getRepository().newInstance();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    public Mono<Boolean> saveUser(Mono<UserEntity> request) {
        return request
                .flatMap(userEntity -> {
                    if (StringUtils.isEmpty(userEntity.getId())) {
                        return doAdd(userEntity);
                    }
                    return findById(userEntity.getId())
                            .flatMap(old -> doUpdate(old, userEntity))
                            .switchIfEmpty(
                                    Objects.equals(userEntity.getId(), userEntity.getUsername()) ?
                                            doAdd(userEntity) :
                                            Mono.error(NotFoundException::new)
                            );
                }).thenReturn(true);
    }

    @Override
    public Mono<UserEntity> addUser(UserEntity userEntity) {
        return doAdd(userEntity);
    }

    protected Mono<UserEntity> doAdd(UserEntity userEntity) {

        return Mono
                .defer(() -> {
                    usernameValidator.validate(userEntity.getUsername());
                    passwordValidator.validate(userEntity.getPassword());
                    userEntity.generateId();
                    userEntity.setSalt(IDGenerator.RANDOM.generate());
                    userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword(), userEntity.getSalt()));
                    return this
                            .createQuery()
                            .where(userEntity::getUsername)
                            .fetch()
                            .doOnNext(u -> {
                                throw new org.hswebframework.web.exception.ValidationException("error.user_already_exists");
                            })
                            .then(Mono.just(userEntity))
                            .as(getRepository()::insert)
                            .onErrorMap(DuplicateKeyException.class, e -> {
                                throw new org.hswebframework.web.exception.ValidationException("error.user_already_exists");
                            })
                            .thenReturn(userEntity)
                            .flatMap(user -> new UserCreatedEvent(user).publish(eventPublisher))
                            .thenReturn(userEntity);
                });

    }


    protected Mono<UserEntity> doUpdate(UserEntity old, UserEntity newer) {
        return Mono
                .defer(() -> {
                    boolean updatePassword = StringUtils.hasText(newer.getPassword());

                    boolean passwordChanged = updatePassword &&
                            !Objects.equals(
                                    passwordEncoder.encode(newer.getPassword(), old.getSalt()),
                                    old.getPassword()
                            );

                    if (updatePassword) {
                        newer.setSalt(IDGenerator.RANDOM.generate());
                        passwordValidator.validate(newer.getPassword());
                        newer.setPassword(passwordEncoder.encode(newer.getPassword(), newer.getSalt()));
                    }
                    return getRepository()
                            .createUpdate()
                            .set(newer)
                            .where(newer::getId)
                            .execute()
                            .flatMap(__ -> new UserModifiedEvent(old, newer, passwordChanged).publish(eventPublisher))
                            .thenReturn(newer)
                            .flatMap(e -> ClearUserAuthorizationCacheEvent
                                    .of(e.getId())
                                    .publish(eventPublisher)
                                    .thenReturn(e));
                });

    }

    @Override
    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    public Mono<UserEntity> findById(String id) {
        return getRepository().findById(Mono.just(id));
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    public Mono<UserEntity> findByUsername(String username) {
        return Mono.justOrEmpty(username)
                   .flatMap(_name -> repository
                           .createQuery()
                           .where(UserEntity::getUsername, _name)
                           .fetchOne());
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    public Mono<UserEntity> findByUsernameAndPassword(String username, String plainPassword) {
        return Mono.justOrEmpty(username)
                   .flatMap(_name -> repository
                           .createQuery()
                           .where(UserEntity::getUsername, _name)
                           .fetchOne())
                   .filter(user -> passwordEncoder
                           .encode(plainPassword, user.getSalt())
                           .equals(user.getPassword()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    public Mono<Integer> changeState(Publisher<String> userId, byte state) {
        return Flux.from(userId)
                   .collectList()
                   .filter(CollectionUtils::isNotEmpty)
                   .flatMap(list -> repository
                           .createUpdate()
                           .set(UserEntity::getStatus, state)
                           .where()
                           .in(UserEntity::getId, list)
                           .execute()
                           .flatMap(i -> UserStateChangedEvent
                                   .of(list, state)
                                   .publish(eventPublisher)
                                   .thenReturn(i)
                           )
                   )
                   .defaultIfEmpty(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    public Mono<Boolean> changePassword(String userId, String oldPassword, String newPassword) {
        passwordValidator.validate(newPassword);
        return findById(userId)
                .switchIfEmpty(Mono.error(NotFoundException::new))
                .filter(user -> passwordEncoder.encode(oldPassword, user.getSalt()).equals(user.getPassword()))
                .switchIfEmpty(Mono.error(() -> new ValidationException("error.illegal_user_password")))
                .flatMap(old -> {
                    String encodePwd = passwordEncoder.encode(newPassword, old.getSalt());

                    boolean passwordChanged = !Objects.equals(encodePwd, old.getPassword());
                    UserEntity newer = old.copyTo(new UserEntity());
                    newer.setPassword(encodePwd);
                    return repository
                            .createUpdate()
                            .set(newer::getPassword)
                            .where(newer::getId)
                            .execute()
                            .flatMap(e -> new UserModifiedEvent(old, newer, passwordChanged)
                                    .publish(eventPublisher)
                                    .thenReturn(e));
                })
                .map(i -> i > 0);
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    public Flux<UserEntity> findUser(QueryParam queryParam) {
        return repository
                .createQuery()
                .setParam(queryParam)
                .fetch();
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    public Mono<Integer> countUser(QueryParam queryParam) {
        return repository
                .createQuery()
                .setParam(queryParam)
                .count();
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    public Mono<Boolean> deleteUser(String userId) {
        return this
                .findById(userId)
                .flatMap(user -> this
                        .deleteById(Mono.just(userId))
                        .flatMap(i -> new UserDeletedEvent(user).publish(eventPublisher))
                        .thenReturn(true));
    }

    @Override
    public Mono<PagerResult<UserEntity>> queryPager(QueryParamEntity queryParamMono) {
        return super.queryPager(queryParamMono);
    }
}
