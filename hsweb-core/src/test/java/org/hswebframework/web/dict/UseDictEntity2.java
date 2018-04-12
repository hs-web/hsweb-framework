package org.hswebframework.web.dict;

import lombok.*;

/**
 * @author zhouhao
 * @since 3.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UseDictEntity2 extends UseDictEntity {
    @Dict(id = "test-code",
            items = {
                    @Item(text = "编码1", value = "1"),
                    @Item(text = "编码2", value = "2")
            })
    private String code;

    private UserCode userCode=UserCode.SIMPLE;
}
