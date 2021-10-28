package org.hswebframework.web.system.authorization.api.event;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.event.DefaultAsyncEvent;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
public class UserStateChangedEvent extends DefaultAsyncEvent {

    private List<String> userIdList;

    private byte state;

}
