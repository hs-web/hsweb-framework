package org.hswebframework.web.starter.easyorm;

import org.springframework.context.annotation.Import;

import javax.persistence.Table;
import java.lang.annotation.*;

/**
 * @see org.hswebframework.ezorm.rdb.mapping.ReactiveRepository
 * @see org.hswebframework.ezorm.rdb.mapping.SyncRepository
 * @since 4.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({EasyormRepositoryRegistrar.class,EasyOrmConfiguration.class})
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
     * @see org.hswebframework.ezorm.rdb.mapping.ReactiveRepository
     */
    boolean enableReactive() default false;

    /**
     * @see org.hswebframework.ezorm.rdb.mapping.SyncRepository
     */
    boolean enableSync() default true;
}
