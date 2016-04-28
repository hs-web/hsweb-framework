package org.hsweb.web.logger;

import com.alibaba.fastjson.JSON;
import org.hsweb.web.bean.po.logger.LoggerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by zhouhao on 16-4-28.
 */
@Component
public class Slf4jAccessLoggerPersisting implements AccessLoggerPersisting {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void save(LoggerInfo loggerInfo) {
        logger.info(JSON.toJSONString(loggerInfo));
    }
}
