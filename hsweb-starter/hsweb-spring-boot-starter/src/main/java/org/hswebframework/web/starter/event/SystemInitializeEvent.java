package org.hswebframework.web.starter.event;

import lombok.Getter;
import org.hswebframework.ezorm.rdb.RDBDatabase;

@Getter
public class SystemInitializeEvent {

    public SystemInitializeEvent(RDBDatabase database){
        this.database=database;
    }

    private RDBDatabase database;

    private boolean ignore;

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

}
