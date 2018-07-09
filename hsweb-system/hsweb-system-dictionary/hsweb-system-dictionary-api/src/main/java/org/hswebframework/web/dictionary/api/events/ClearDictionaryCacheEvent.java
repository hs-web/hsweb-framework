package org.hswebframework.web.dictionary.api.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@AllArgsConstructor
@Getter
public class ClearDictionaryCacheEvent {
    private String dictionaryId;
}
