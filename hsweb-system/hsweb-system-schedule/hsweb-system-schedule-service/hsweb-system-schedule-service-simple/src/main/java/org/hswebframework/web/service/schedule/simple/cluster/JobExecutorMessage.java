package org.hswebframework.web.service.schedule.simple.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hswebframework.web.message.Message;

import java.util.Map;

/**
 * @author zhouhao
 * @since 3.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobExecutorMessage implements Message {

    private String executeId;

    private String jobId;

    private Map<String, Object> parameters;
}
