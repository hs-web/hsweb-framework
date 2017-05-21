package org.hswebframework.web.service.dictionary.parser;

import java.io.Serializable;
import java.util.Optional;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface SingleDictParser extends Serializable {
    Optional<String> parse(String target, Object context);

    default Optional<String> parse(String target) {
        return parse(target, null);
    }
}
