package org.hswebframework.web.audit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Consumer;

/**
 * 审计信息注解,用于在修改更新数据的时候记录被修改的内容.
 *
 * @since 3.0.7
 */
@Getter
@Setter
public class AuditMeta {

    private String type;

    private String comment;

    private List<AuditFieldMeta> fields;

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(fields);
    }
}
