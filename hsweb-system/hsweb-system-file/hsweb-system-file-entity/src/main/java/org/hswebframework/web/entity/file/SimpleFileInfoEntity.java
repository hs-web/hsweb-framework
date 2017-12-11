package org.hswebframework.web.entity.file;

import lombok.*;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

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

}