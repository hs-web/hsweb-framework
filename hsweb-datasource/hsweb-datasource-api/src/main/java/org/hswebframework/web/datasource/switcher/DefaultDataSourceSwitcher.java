package org.hswebframework.web.datasource.switcher;

import org.hswebframework.web.ThreadLocalUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class DefaultDataSourceSwitcher implements DataSourceSwitcher {
    private static final String DEFAULT_DATASOURCE_ID = DataSourceSwitcher.class.getName() + "_default_";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Deque<String> getUsedHistoryQueue() {
        return ThreadLocalUtils.get(DefaultDataSourceSwitcher.class.getName() + "_queue", LinkedList::new);
    }

    @Override
    public void useLast() {
        if (getUsedHistoryQueue().size() == 0) {
            return;
        }
        getUsedHistoryQueue().removeLast();

        if (logger.isDebugEnabled()) {
            String current = currentDataSourceId();
            if (null != current)
                logger.debug("try use last data source : {}", currentDataSourceId());
            else logger.debug("try use default data source");
        }
    }

    @Override
    public void use(String dataSourceId) {
        getUsedHistoryQueue().addLast(dataSourceId);
        if (logger.isDebugEnabled()) {
            logger.debug("try use data source : {}", dataSourceId);
        }
    }

    @Override
    public void useDefault() {
        getUsedHistoryQueue().addLast(DEFAULT_DATASOURCE_ID);
        if (logger.isDebugEnabled()) {
            logger.debug("try use default data source");
        }
    }

    @Override
    public String currentDataSourceId() {
        if (getUsedHistoryQueue().size() == 0) return null;

        String activeId = getUsedHistoryQueue().getLast();
        if (DEFAULT_DATASOURCE_ID.equals(activeId)) {
            return null;
        }
        return activeId;
    }

    @Override
    public void reset() {
        getUsedHistoryQueue().clear();
        if (logger.isDebugEnabled()) {
            logger.debug("reset data source used history");
        }
    }
}
