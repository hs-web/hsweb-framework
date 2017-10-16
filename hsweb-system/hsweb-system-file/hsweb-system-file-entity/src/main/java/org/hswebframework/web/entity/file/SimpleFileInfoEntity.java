package org.hswebframework.web.entity.file;

import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * 文件信息
 *
 * @author hsweb-generator-online
 */
public class SimpleFileInfoEntity extends SimpleGenericEntity<String> implements FileInfoEntity {
    //文件名称
    private String name;
    //路径
    private String location;
    //类型
    private String type;
    //md5校验值
    private String md5;
    //文件大小
    private Long   size;
    //状态
    private Byte   status;
    //分类
    private String classified;
    //创建时间
    private Long   createTime;
    //创建人
    private String creatorId;

    /**
     * @return 文件名称
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @param name 文件名称
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return 类型
     */
    @Override
    public String getType() {
        return this.type;
    }

    /**
     * @param type 类型
     */
    @Override
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return md5校验值
     */
    @Override
    public String getMd5() {
        return this.md5;
    }

    /**
     * @param md5 md5校验值
     */
    @Override
    public void setMd5(String md5) {
        this.md5 = md5;
    }

    /**
     * @return 文件大小
     */
    @Override
    public Long getSize() {
        return this.size;
    }

    /**
     * @param size 文件大小
     */
    @Override
    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * @return 状态
     */
    @Override
    public Byte getStatus() {
        return this.status;
    }

    /**
     * @param status 状态
     */
    @Override
    public void setStatus(Byte status) {
        this.status = status;
    }

    /**
     * @return 分类
     */
    @Override
    public String getClassified() {
        return this.classified;
    }

    /**
     * @param classified 分类
     */
    @Override
    public void setClassified(String classified) {
        this.classified = classified;
    }

    /**
     * @return 创建人
     */
    @Override
    public String getCreatorId() {
        return this.creatorId;
    }

    @Override
    public Long getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    /**
     * @param creatorId 创建人
     */
    @Override
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
}