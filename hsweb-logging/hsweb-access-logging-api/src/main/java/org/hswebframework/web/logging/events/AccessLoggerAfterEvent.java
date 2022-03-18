package org.hswebframework.web.logging.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.event.DefaultAsyncEvent;
import org.hswebframework.web.logging.AccessLoggerInfo;

@AllArgsConstructor
@Getter
public class AccessLoggerAfterEvent extends DefaultAsyncEvent {

    private AccessLoggerInfo logger;
}
