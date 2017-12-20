package org.hswebframework.web.datasource.exception;

import org.hswebframework.web.NotFoundException;

/**
 * @author zhouhao
 */
public class DataSourceClosedException extends NotFoundException {

    private static final long serialVersionUID = 7474086353335778733L;
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
