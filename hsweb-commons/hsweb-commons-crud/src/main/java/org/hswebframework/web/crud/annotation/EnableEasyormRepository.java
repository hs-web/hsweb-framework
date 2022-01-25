package org.hswebframework.web.crud.annotation;

import org.hswebframework.web.crud.configuration.EasyormRepositoryRegistrar;
import org.springframework.context.annotation.Import;

import javax.persistence.Table;
import java.lang.annotation.*;

/**
 * 在启动类上注解,标识开启自动注册实体通用增删改查接口到spring上下文中.
 * 在spring中,可直接进行泛型注入使用:
 * <pre>{@code
 *   @Autowire
 *   ReactiveRepository<String, MyEntity> repository;
 * }</pre>
 *
 * @see org.hswebframework.ezorm.rdb.mapping.ReactiveRepository
 * @see org.hswebframework.ezorm.rdb.mapping.SyncRepository
 * @since 4.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({EasyormRepositoryRegistrar.class})
public @interface EnableEasyormRepository {

    /**
     * 实体类包名:
     * <pre>
     *     com.company.project.entity
     * </pre>
     */
    String[] value();

    /**
     * @see org.hswebframework.ezorm.rdb.mapping.jpa.JpaEntityTableMetadataParser
     */
    Class<? extends Annotation>[] annotation() default Table.class;

    /**
     * @return 是否开启响应式, 默认开启
     */
    boolean reactive() default true;

    /**
     * 是否开启非响应式操作,在使用WebFlux时,不建议开启
     *
     * @return 开启非响应式
     * @see org.hswebframework.ezorm.rdb.mapping.SyncRepository
     */
    boolean nonReactive() default false;

}
