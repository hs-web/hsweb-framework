package org.hswebframework.web.commons.entity;

/**
 * @author zhouhao
 * @see DataStatusEnum
 */
public interface DataStatus {
    Byte STATUS_ENABLED = 1;
    Byte STATUS_DISABLED = 0;
    Byte STATUS_LOCKED = -1;
}
