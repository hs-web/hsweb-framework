package org.hswebframework.web.task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobDetail {

    private String id;

    private String name;

    private String description;

    private String type;

    private long executeTimeOut;

    private long retryTimes;

    private long retryInterval;

    private boolean parallel;

    private String content;

}
