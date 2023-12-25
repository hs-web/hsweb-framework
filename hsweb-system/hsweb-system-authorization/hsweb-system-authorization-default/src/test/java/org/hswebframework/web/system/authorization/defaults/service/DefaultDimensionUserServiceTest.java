package org.hswebframework.web.system.authorization.defaults.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.ReactiveAuthenticationManager;
import org.hswebframework.web.system.authorization.api.entity.*;
import org.hswebframework.web.system.authorization.api.service.reactive.ReactiveUserService;
import org.hswebframework.web.system.authorization.defaults.service.reactive.ReactiveTestApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        ReactiveTestApplication.class,
        DefaultReactiveUserService.class
})
public class DefaultDimensionUserServiceTest {

    @Autowired
    private ReactiveUserService userService;

    @Autowired
    private ReactiveRepository<DimensionTypeEntity, String> typeRepository;

    @Autowired
    private DefaultDimensionService dimensionService;

    @Autowired
    private DefaultDimensionUserService dimensionUserService;

    @Autowired
    private DefaultAuthorizationSettingService settingService;

    @Autowired
    private ReactiveAuthenticationManager authenticationManager;

    @Test
    public void testDeleteBind() {
        String dimensionType = "role";
        String dimensionId = "testDeleteBind";
        String userId = initData(dimensionType, dimensionId);

        //删除绑定关系
        dimensionUserService
                .createDelete()
                .where(DimensionUserEntity::getUserId, userId)
                .execute()
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();

        //校验权限
        authenticationManager
                .getByUserId(userId)
                .map(auth -> !auth.hasDimension(dimensionType, dimensionId))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        //权限设置并没有被删除
        settingService
                .createQuery()
                .where(AuthorizationSettingEntity::getDimensionType, dimensionType)
                .and(AuthorizationSettingEntity::getDimensionTarget, dimensionId)
                .count()
                .as(StepVerifier::create)
                .expectNext(1)
                .verifyComplete();

    }

    @Test
    public void testDeleteDimension() {
        String dimensionType = "role";
        String dimensionId = "testDeleteDimension";
        String userId = initData(dimensionType, dimensionId);

        //删除维度
        dimensionService
                .deleteById(dimensionId)
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();

        //判断没有维度
        authenticationManager
                .getByUserId(userId)
                .map(auth -> !auth.hasDimension(dimensionType, dimensionId))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        //权限设置也被删除
        settingService
                .createQuery()
                .where(AuthorizationSettingEntity::getDimensionType, dimensionType)
                .and(AuthorizationSettingEntity::getDimensionTarget, dimensionId)
                .count()
                .as(StepVerifier::create)
                .expectNext(0)
                .verifyComplete();


    }

    @Test
    public void testDeleteUser() {
        String dimensionType = "role";
        String dimensionId = "test";

        String userId = initData(dimensionType, dimensionId);

        userService.deleteUser(userId)
                   .as(StepVerifier::create)
                   .expectNext(true)
                   .verifyComplete();

        authenticationManager
                .getByUserId(userId)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();


        dimensionUserService
                .createQuery()
                .where(DimensionUserEntity::getUserId, userId)
                .count()
                .as(StepVerifier::create)
                .expectNext(0)
                .verifyComplete();

    }


    private String initData(String dimensionType, String dimensionId) {
        UserEntity userEntity = userService.newUserInstance().blockOptional().orElseThrow(NullPointerException::new);
        userEntity.setName("test");
        userEntity.setUsername("test_" + dimensionId);
        userEntity.setPassword("admin");
        userService.saveUser(Mono.just(userEntity))
                   .as(StepVerifier::create)
                   .expectNext(true)
                   .verifyComplete();

        DimensionTypeEntity type = new DimensionTypeEntity();
        type.setId(dimensionType);
        type.setName(dimensionType);
        typeRepository.save(type)
                      .then()
                      .as(StepVerifier::create)
                      .expectComplete()
                      .verify();

        DimensionEntity dimension = new DimensionEntity();
        dimension.setId(dimensionId);
        dimension.setTypeId(dimensionType);
        dimension.setName(dimensionId);

        dimensionService
                .save(dimension)
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();

        DimensionUserEntity bind = new DimensionUserEntity();
        bind.setDimensionId(dimension.getId());
        bind.setDimensionTypeId(dimension.getTypeId());
        bind.setDimensionName(dimension.getName());
        bind.setUserId(userEntity.getId());
        bind.setUserName(userEntity.getName());
        dimensionUserService
                .save(bind)
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();

        AuthorizationSettingEntity setting = new AuthorizationSettingEntity();
        setting.setDimensionType(dimension.getTypeId());
        setting.setDimensionTarget(dimension.getId());
        setting.setPermission("test");

        settingService
                .insert(setting)
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();

        authenticationManager
                .getByUserId(userEntity.getId())
                .map(auth -> auth.hasDimension(dimensionType, dimensionId))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        return userEntity.getId();
    }

}