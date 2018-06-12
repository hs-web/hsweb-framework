package org.hswebframework.web.excel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;
import java.util.List;
import java.util.function.Function;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface ExcelImporter {

    ExcelImporter instance = new DefaultExcelImporter();

    <T> Result<T> doImport(InputStream inputStream, Class<T> type, Function<T, Error> validator, Class... group);

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
