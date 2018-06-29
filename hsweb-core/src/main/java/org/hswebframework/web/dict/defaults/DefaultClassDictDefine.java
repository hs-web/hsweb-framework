package org.hswebframework.web.dict.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hswebframework.web.dict.ClassDictDefine;
import org.hswebframework.web.dict.EnumDict;
import org.hswebframework.web.dict.ItemDefine;

import java.util.List;

/**
 * @author zhouhao
 * @since 3.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultClassDictDefine implements ClassDictDefine {
    private static final long serialVersionUID = -4113467848927281082L;
    private String                 field;
    private String                 id;
    private String                 alias;
    private String                 comments;
    private String                 parserId;
    private List<EnumDict<Object>> items;
}
