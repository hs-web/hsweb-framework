package org.hswebframework.web.service.schedule.simple.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hswebframework.web.message.Message;

/**
 * @author zhouhao
 * @since 3.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobExecuteResultMessage implements Message {
    private String executeId;

    private boolean success;

    private Object  result;

}
