package org.hswebframework.web.excel;

import java.lang.annotation.*;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Excel {

    String value() default "";

    int sheetIndex() default -1;

    boolean ignore() default false;

    boolean enableImport() default true;

    boolean enableExport() default true;

    int exportOrder() default -1;

    Class[] group() default Void.class;

    Class<ExcelCellConverter> converter() default ExcelCellConverter.class;

    Class<ExcelImporter> importer() default ExcelImporter.class;

}
