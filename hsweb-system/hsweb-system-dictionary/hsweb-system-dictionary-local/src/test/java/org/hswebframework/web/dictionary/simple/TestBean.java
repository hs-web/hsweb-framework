package org.hswebframework.web.dictionary.simple;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author zhouhao
 * @since
 */
@Data
@Getter
@Setter
@ToString
public class TestBean {
    private TestDict[] dict;

    private TestDict dict2;
}
