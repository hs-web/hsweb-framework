package org.hswebframework.web.entity.file;

import lombok.*;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * 文件信息
 *
 * @author hsweb-generator-online
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "s_file_info", indexes = {
        @Index(name = "idx_file_md5", columnList = "md5")
})
public class SimpleFileInfoEntity extends SimpleGenericEntity<String> implements FileInfoEntity {
    //文件名称
    @Column
    private String name;
    //路径
    @Column(length = 2048)
    private String location;
    //类型
    @Column
    private String type;
    //md5校验值
    @Column(length = 64)
    private String md5;
    //文件大小
    @Column
    private Long size;
    //状态
    @Column
    private Byte status;
    //分类
    @Column
    private String classified;
    //创建时间
    @Column(name = "create_time")
    private Long createTime;
    //创建人
    @Column(name = "creator_id")
    private String creatorId;

}