package org.hswebframework.web.crud.annotation;

import org.hswebframework.web.crud.configuration.EasyormRepositoryRegistrar;
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


}
