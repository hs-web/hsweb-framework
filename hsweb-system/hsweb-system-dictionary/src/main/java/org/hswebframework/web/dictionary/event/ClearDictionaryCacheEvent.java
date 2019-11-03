package org.hswebframework.web.dictionary.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author zhouhao
 */
@AllArgsConstructor(staticName = "of")
@Getter
@NoArgsConstructor(staticName = "of")
public class ClearDictionaryCacheEvent {
    private String dictionaryId;
}
