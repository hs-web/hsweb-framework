package org.hswebframework.web.datasource.exception;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class DataSourceNotFoundException extends RuntimeException {

    private String dataSourceId;

    public DataSourceNotFoundException(String dataSourceId) {
        this(dataSourceId, dataSourceId);
    }

    public DataSourceNotFoundException(String dataSourceId, String message) {
        super(message);
        this.dataSourceId = dataSourceId;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

}
