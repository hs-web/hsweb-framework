package org.hswebframework.web.service.form.initialize;

/**
 * @author zhouhao
 * @since 3.0
 */
public interface DynamicFormInitializeCustomer {
    void customTableSetting(TableInitializeContext context);

    void customTableColumnSetting(ColumnInitializeContext context);

}
