package org.hswebframework.web.service.script.simple;

import lombok.*;
import org.hswebframework.web.message.Message;

import java.util.Map;

/**
 *
 * @author zhouhao
 * @since 3.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ScriptExecutorMessage implements Message{
    private String scriptId;

    private Map<String,Object> parameter;

    private String callback;
}
