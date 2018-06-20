package org.hswebframework.web.excel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.bean.FastBeanCopier;

import java.io.InputStream;
import java.util.List;
import java.util.function.Function;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface ExcelImporter {

    ExcelImporter instance = new DefaultExcelImporter();

    /**
     * 解析excel为指定的class对象,并返回解析结果.类上的属性需要注解{@link Excel}或者{@link io.swagger.annotations.ApiModelProperty}.
     *
     * @param inputStream excel文件流,支持xls和xlsx
     * @param type        要解析为的类型
     * @param afterParsed 每解析完一个对象都会调用此接口,用于自定义操作,如: 数据校验
     * @param group       导入的分组 {@link Excel#group()},如果不指定则为 {@link Void#getClass()}
     * @param <T>         泛型
     * @return 导入结果, 包含了成功, 失败信息
     * @see Excel
     * @see FastBeanCopier#getBeanFactory()
     * @see ExcelImporter#instance 默认的实现
     */
    <T> Result<T> doImport(InputStream inputStream, Class<T> type, Function<T, Error> afterParsed, Class... group);

    @Builder
    @Getter
    @Setter
    class Result<T> {
        int          total;
        int          success;
        int          error;
        List<Header> headers;
        List<T>      data;
        List<Error>  errors;
    }

    @Builder
    @Getter
    @Setter
    class Error {
        int    sheetIndex;
        int    rowIndex;
        int    errorType;
        Object reason;
    }

    @Builder
    @Getter
    @Setter
    class Header {
        int sheetIndex;
        @SuppressWarnings("all")
        String header;
        String field;
    }
}
