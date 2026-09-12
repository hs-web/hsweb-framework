package org.hswebframework.web.datasource.switcher;

import lombok.extern.slf4j.Slf4j;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

@Slf4j
public class DefaultSwitcher implements Switcher {

    private String name;

    private String defaultId;

    private String type;

    private final ThreadLocal<Deque<String>> usedHistory = ThreadLocal.withInitial(LinkedList::new);

    public DefaultSwitcher(String name, String type) {
        this.name = "DefaultSwitcher.".concat(name);
        this.defaultId = name.concat(".").concat("_default");
        this.type = type;
    }

    protected Deque<String> getUsedHistoryQueue() {
        // 同步切换场景仍然基于 ThreadLocal 保存上下文历史，
        // reactive 场景由 DefaultReactiveSwitcher 单独承载。
        return usedHistory.get();
    }

    @Override
    public void useLast() {
        // 没有上一次了
        if (getUsedHistoryQueue().isEmpty()) {
            return;
        }
        //移除队尾,则当前的队尾则为上一次的数据源
        getUsedHistoryQueue().removeLast();
        if (log.isDebugEnabled()) {
            String current = current().orElse(null);
            if (null != current) {
                log.debug("try use last {} : {}", type, current);
            } else {
                log.debug("try use last default {}", type);
            }
        }
    }

    @Override
    public void use(String id) {
        //添加对队尾
        getUsedHistoryQueue().addLast(id);
        if (log.isDebugEnabled()) {
            log.debug("try use {} : {}", type, id);
        }
    }

    @Override
    public void useDefault() {
        getUsedHistoryQueue().addLast(defaultId);
        if (log.isDebugEnabled()) {
            log.debug("try use default {}", type);
        }
    }

    @Override
    public Optional<String> current() {
        if (getUsedHistoryQueue().isEmpty()) {
            return Optional.empty();
        }

        String activeId = getUsedHistoryQueue().getLast();
        if (defaultId.equals(activeId)) {
            return Optional.empty();
        }
        return Optional.of(activeId);
    }

    @Override
    public void reset() {
        usedHistory.remove();
        if (log.isDebugEnabled()) {
            log.debug("reset {} history", type);
        }
    }
}
