package org.hswebframework.web.example.simple;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.Item;

/**
 * @author zhouhao
 * @since 3.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestEntity {

    @Dict(alias = "sexText", items = {
            @Item(text = "男", value = "1"),
            @Item(text = "女", value = "2")
    })
    private Byte sex;

    private String sexText;
}
