package org.hswebframework.web.system.authorization.defaults.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.crud.service.GenericCrudService;
import org.hswebframework.web.exception.NotFoundException;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.system.authorization.api.PasswordEncoder;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.hswebframework.web.system.authorization.api.event.UserCreatedEvent;
import org.hswebframework.web.system.authorization.api.event.UserModifiedEvent;
import org.hswebframework.web.system.authorization.api.service.UserService;
import org.hswebframework.web.validator.CreateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import javax.validation.ValidationException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Transactional(rollbackFor = Exception.class)
public class DefaultUserService extends GenericCrudService<UserEntity, String> implements UserService {

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder = (password, salt) -> DigestUtils.md5Hex(String.format("hsweb.%s.framework.%s", password, salt));

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public boolean saveUser(UserEntity userEntity) {
        if (StringUtils.isEmpty(userEntity.getId())) {
            return doAdd(userEntity);
        }
        UserEntity old = findById(userEntity.getId()).orElse(null);
        if (old == null) {
            return doAdd(userEntity);
        }

        return doUpdate(userEntity);
    }

    protected boolean doAdd(UserEntity userEntity) {
        userEntity.tryValidate(CreateGroup.class);
        userEntity.setStatus((byte)1);

        userEntity.setSalt(IDGenerator.RANDOM.generate());
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword(), userEntity.getSalt()));
        if (createQuery()
                .where(userEntity::getUsername)
                .count() > 0) {
            throw new ValidationException("用户名已存在");
        }
        getRepository().insert(userEntity);
        eventPublisher.publishEvent(new UserCreatedEvent(userEntity));
        return true;
    }


    protected boolean doUpdate(UserEntity userEntity) {
        boolean passwordChanged = StringUtils.hasText(userEntity.getPassword());
        if (passwordChanged) {
            userEntity.setSalt(IDGenerator.RANDOM.generate());
            userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword(), userEntity.getSalt()));
        }
        getRepository()
                .createUpdate()
                .set(userEntity)
                .where(userEntity::getId)
                .execute();

        eventPublisher.publishEvent(new UserModifiedEvent(userEntity, passwordChanged));
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserEntity> findById(Collection<String> id) {
        return super.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserEntity> findById(String id) {
        return super.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserEntity> findByUsername(String username) {
        return createQuery()
                .where(UserEntity::getUsername, username)
                .fetchOne();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserEntity> findByUsernameAndPassword(String username, String plainPassword) {
        return findByUsername(username)
                .filter(user -> passwordEncoder.encode(plainPassword, user.getSalt()).equals(user.getPassword()));
    }

    @Override
    public boolean changeState(String userId, byte state) {
        return createUpdate()
                .where(UserEntity::getId, userId)
                .set(UserEntity::getStatus, state)
                .execute() > 0;
    }

    @Override
    public void changePassword(String userId, String oldPassword, String newPassword) {
        Mono.justOrEmpty(findById(userId))
                .switchIfEmpty(Mono.error(NotFoundException::new))
                .filter(user -> passwordEncoder.encode(oldPassword, user.getSalt()).equals(user.getPassword()))
                .switchIfEmpty(Mono.error(() -> new ValidationException("密码错误")))
                .map(user -> createUpdate()
                        .set(UserEntity::getPassword, passwordEncoder.encode(newPassword, user.getSalt()))
                        .where(user::getId).execute())
                .block();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserEntity> findUser(QueryParam queryParam) {
        return createQuery().setParam(queryParam).fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public long countUser(QueryParam queryParam) {
        return createQuery().setParam(queryParam).count();
    }

    @Override
    public PagerResult<UserEntity> findUserPager(QueryParam param) {
        return queryPager(param);
    }
}
