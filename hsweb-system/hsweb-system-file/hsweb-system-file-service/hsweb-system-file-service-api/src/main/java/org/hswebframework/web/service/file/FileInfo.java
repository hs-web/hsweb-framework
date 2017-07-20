package org.hswebframework.web.service.file;

import java.io.Serializable;

/**
 * Created by zhouhao on 2017/7/8.
 */
public interface FileInfo  extends Serializable{

    String getId();

    void setId(String id);

    String getName();

    long getLength();

    long getCreateTime();

    String getCreatorId();

    String getMd5();

    String getLocation();


}
