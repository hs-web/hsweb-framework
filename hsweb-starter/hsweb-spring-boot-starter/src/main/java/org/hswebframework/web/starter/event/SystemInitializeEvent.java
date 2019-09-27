package org.hswebframework.web.starter.event;

import lombok.Getter;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;

@Getter
public class SystemInitializeEvent {

    public SystemInitializeEvent(DatabaseOperator database){
        this.database=database;
    }

    private DatabaseOperator database;

    private boolean ignore;

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

}
