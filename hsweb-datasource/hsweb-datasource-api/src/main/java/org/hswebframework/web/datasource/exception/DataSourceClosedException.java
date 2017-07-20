package org.hswebframework.web.datasource.exception;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class DataSourceClosedException extends RuntimeException {

    private String dataSourceId;

    public DataSourceClosedException(String dataSourceId) {
        this(dataSourceId, dataSourceId);
    }

    public DataSourceClosedException(String dataSourceId, String message) {
        super(message);
        this.dataSourceId = dataSourceId;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

}
