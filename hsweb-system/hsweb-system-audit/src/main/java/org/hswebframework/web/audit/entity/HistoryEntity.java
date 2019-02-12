package org.hswebframework.web.audit.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryEntity {

    private String batchId;

    private String dataId;

    private String entity;

    private String entityName;

    private String property;

    private String propertyName;

    private String comment;

    private String action;

    private String before;

    private String after;

    private Long createTime;

    private Long operationUserId;



}
