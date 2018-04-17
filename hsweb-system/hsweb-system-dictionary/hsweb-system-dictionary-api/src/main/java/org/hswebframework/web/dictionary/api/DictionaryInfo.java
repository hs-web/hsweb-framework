package org.hswebframework.web.dictionary.api;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.bean.ValidateBean;

import java.io.Serializable;

/**
 * 字典信息
 *
 * @author zhouhao
 * @since 3.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DictionaryInfo implements ValidateBean, Serializable {
    private static final long serialVersionUID = -4017149592047646129L;

    private String id;

    @NotBlank(message = "[字典ID]不能为空")
    private String dictionaryId;

    @NotBlank(message = "[目标key]不能为空")
    private String targetKey;

    @NotBlank(message = "[目标ID]不能为空")
    private String targetId;

    @NotBlank(message = "[值]不能为空")
    private String value;

    @NotBlank(message = "[文本说明]不能为空")
    private String text;

}
