package org.hswebframework.web.service.script.simple;

import lombok.*;
import org.hswebframework.web.message.Message;

/**
 * @author zhouhao
 * @since 3.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptExecutorResultMessage implements Message{
    private Object result;
}
