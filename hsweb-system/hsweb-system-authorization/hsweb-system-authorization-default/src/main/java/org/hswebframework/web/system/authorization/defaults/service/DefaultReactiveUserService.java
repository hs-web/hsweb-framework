package org.hswebframework.web.system.authorization.defaults.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.api.crud.entity.TransactionManagers;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.exception.NotFoundException;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.system.authorization.api.PasswordEncoder;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.hswebframework.web.system.authorization.api.event.UserCreatedEvent;
import org.hswebframework.web.system.authorization.api.event.UserDeletedEvent;
import org.hswebframework.web.system.authorization.api.event.UserModifiedEvent;
import org.hswebframework.web.system.authorization.api.service.reactive.ReactiveUserService;
import org.hswebframework.web.validator.CreateGroup;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ValidationException;


public class DefaultReactiveUserService extends GenericReactiveCrudService<UserEntity, String> implements ReactiveUserService {

    @Autowired
    private ReactiveRepository<UserEntity, String> repository;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder = (password, salt) -> DigestUtils.md5Hex(String.format("hsweb.%s.framework.%s", password, salt));

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public Mono<UserEntity> newUserInstance() {
        return getRepository().newInstance();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = TransactionManagers.r2dbcTransactionManager)
    public Mono<Boolean> saveUser(Mono<UserEntity> request) {
        return request
                .flatMap(userEntity -> {
                    if (StringUtils.isEmpty(userEntity.getId())) {
                        return doAdd(userEntity);
                    }
                    return findById(userEntity.getId())
                            .flatMap(ignore -> doUpdate(userEntity))
                            .switchIfEmpty(doAdd(userEntity));
                }).thenReturn(true);
    }

    protected Mono<UserEntity> doAdd(UserEntity userEntity) {

        return Mono.defer(() -> {
            userEntity.setSalt(IDGenerator.RANDOM.generate());
            userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword(), userEntity.getSalt()));
            return Mono.just(userEntity)
                    .doOnNext(e -> e.tryValidate(CreateGroup.class))
                    .filterWhen(e -> createQuery()
                            .where(userEntity::getUsername)
                            .count().map(i -> i == 0))
                    .switchIfEmpty(Mono.error(() -> new ValidationException("用户名已存在")))
                    .as(getRepository()::insert)
                    .thenReturn(userEntity)
                    .doOnSuccess(e -> eventPublisher.publishEvent(new UserCreatedEvent(e)));
        });

    }


    protected Mono<UserEntity> doUpdate(UserEntity userEntity) {
        return Mono.defer(() -> {
            boolean passwordChanged = StringUtils.hasText(userEntity.getPassword());
            if (passwordChanged) {
                userEntity.setSalt(IDGenerator.RANDOM.generate());
                userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword(), userEntity.getSalt()));
            }
            return getRepository()
                    .createUpdate()
                    .set(userEntity)
                    .where(userEntity::getId)
                    .execute()
                    .doOnSuccess(__ -> eventPublisher.publishEvent(new UserModifiedEvent(userEntity, passwordChanged)))
                    .thenReturn(userEntity);
        });

    }

    @Override
    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    public Mono<UserEntity> findById(String id) {
        return getRepository().findById(Mono.just(id));
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    public Mono<UserEntity> findByUsername(String username) {
        return Mono.justOrEmpty(username)
                .flatMap(_name -> repository.createQuery()
                        .where(UserEntity::getUsername, _name)
                        .fetchOne());
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    public Mono<UserEntity> findByUsernameAndPassword(String username, String plainPassword) {
        return Mono.justOrEmpty(username)
                .flatMap(_name -> repository
                        .createQuery()
                        .where(UserEntity::getUsername, _name)
                        .fetchOne())
                .filter(user -> passwordEncoder.encode(plainPassword, user.getSalt())
                        .equals(user.getPassword()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = TransactionManagers.r2dbcTransactionManager)
    public Mono<Integer> changeState(Publisher<String> userId, byte state) {
        return Flux.from(userId)
                .collectList()
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(list -> repository
                        .createUpdate()
                        .set(UserEntity::getStatus, state)
                        .where()
                        .in(UserEntity::getId, list)
                        .execute())
                .defaultIfEmpty(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = TransactionManagers.r2dbcTransactionManager)
    public Mono<Boolean> changePassword(String userId, String oldPassword, String newPassword) {
        return findById(userId)
                .switchIfEmpty(Mono.error(NotFoundException::new))
                .filter(user -> passwordEncoder.encode(oldPassword, user.getSalt()).equals(user.getPassword()))
                .switchIfEmpty(Mono.error(() -> new ValidationException("密码错误")))
                .flatMap(user -> repository.createUpdate()
                        .set(UserEntity::getPassword, passwordEncoder.encode(newPassword, user.getSalt()))
                        .where(user::getId)
                        .execute())
                .map(i -> i > 0);
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    public Flux<UserEntity> findUser(QueryParam queryParam) {
        return repository
                .createQuery()
                .setParam(queryParam)
                .fetch();
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    public Mono<Integer> countUser(QueryParam queryParam) {
        return repository
                .createQuery()
                .setParam(queryParam)
                .count();
    }

    @Override
    public Mono<Boolean> deleteUser(String userId) {
        return this.findById(userId)
                .flatMap(user -> this
                        .deleteById(Mono.just(userId))
                        .doOnNext(i -> eventPublisher.publishEvent(new UserDeletedEvent(user)))
                        .thenReturn(true));
    }
}
