package org.hswebframework.web.excel;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface ExcelCellConverter<T> {
    T convertFromCell(Object from);

    Object convertToCell(T from);
}
