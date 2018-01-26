package org.hswebframework.web.dict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhouhao
 * @since 3.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UseDictEntity {
    @Dict(
            alias = "statusText"
            , items = {
            @Item(text = "正常", value = "1"),
            @Item(text = "失效", value = "0")
    })
    private Byte status;

    private String statusText;
}
