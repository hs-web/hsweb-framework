package org.hswebframework.web.excel;

import io.swagger.annotations.ApiModelProperty;

import java.lang.annotation.*;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Excel {

    /**
     * @return EXCEL表头
     * @see ApiModelProperty#value()
     */
    String value() default "";

    /**
     * @return 读取指定的工作薄,-1为默认
     */
    int sheetIndex() default -1;

    /**
     * @return 是否取消EXCEL导入导出功能
     */
    boolean ignore() default false;

    /**
     * @return 是否开启导入, 开启后的字段才能进行导入
     */
    boolean enableImport() default true;

    /**
     * @return 是否开启导出, 开启后的字段才能进行导出
     */
    boolean enableExport() default true;

    /**
     * @return 导出时, 表头的顺序
     */
    int exportOrder() default -1;

    /**
     * @return 导出分组, 可通过分组导入导出不同的字段信息
     */
    Class[] group() default Void.class;

    /**
     * 自定义单元格转换器, 用于对数据字典等字段进行自定义转换
     *
     * @return 实例必须注入到spring容器中
     * @see org.springframework.context.ApplicationContext#getBean(Class)
     */
    Class<ExcelCellConverter> converter() default ExcelCellConverter.class;

}
