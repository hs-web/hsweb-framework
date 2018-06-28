package org.hswebframework.web.dict.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class DefaultItemDefine implements ItemDefine {
    private String text;
    private String value;
    private String comments;
    private int ordinal;
    private List<ItemDefine> children;
}
