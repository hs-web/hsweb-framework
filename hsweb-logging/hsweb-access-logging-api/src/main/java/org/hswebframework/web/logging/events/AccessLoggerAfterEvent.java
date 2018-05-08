package org.hswebframework.web.logging.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.logging.AccessLoggerInfo;

@AllArgsConstructor
@Getter
public class AccessLoggerAfterEvent {

    private AccessLoggerInfo logger;
}
