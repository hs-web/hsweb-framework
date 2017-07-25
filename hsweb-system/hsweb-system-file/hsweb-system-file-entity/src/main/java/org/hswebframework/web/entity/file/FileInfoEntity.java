package org.hswebframework.web.entity.file;

import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.commons.entity.RecordCreationEntity;

/**
 * 文件信息 实体
 *
 * @author hsweb-generator-online
 */
public interface FileInfoEntity extends RecordCreationEntity, GenericEntity<String> {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 文件名称
     */
    String name       = "name";
    /**
     * 文件相对路径
     */
    String location   = "location";
    /**
     * 类型
     */
    String type       = "type";
    /**
     * md5校验值
     */
    String md5        = "md5";
    /**
     * 文件大小
     */
    String size       = "size";
    /**
     * 状态
     */
    String status     = "status";
    /**
     * 分类
     */
    String classified = "classified";

    /**
     * @return 文件名称
     */
    String getName();

    /**
     * @param name 文件名称
     */
    void setName(String name);

    /**
     * @return 路径
     */
    String getLocation();

    /**
     * @param location 文件相对路径
     */
    void setLocation(String location);

    /**
     * @return 类型
     */
    String getType();

    /**
     * @param type 类型
     */
    void setType(String type);

    /**
     * @return md5校验值
     */
    String getMd5();

    /**
     * @param md5 md5校验值
     */
    void setMd5(String md5);

    /**
     * @return 文件大小
     */
    Long getSize();

    /**
     * @param size 文件大小
     */
    void setSize(Long size);

    /**
     * @return 状态
     */
    Byte getStatus();

    /**
     * @param status 状态
     */
    void setStatus(Byte status);

    /**
     * @return 分类
     */
    String getClassified();

    /**
     * @param classified 分类
     */
    void setClassified(String classified);


}