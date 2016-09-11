package org.hsweb.web.service.impl.history;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.history.History;
import org.hsweb.web.dao.history.HistoryMapper;
import org.hsweb.web.service.history.HistoryService;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zhouhao on 16-4-22.
 */
@Service("historyService")
public class HistoryServiceImpl extends AbstractServiceImpl<History, String> implements HistoryService {
    @Resource
    public HistoryMapper historyMapper;

    @Override
    protected HistoryMapper getMapper() {
        return historyMapper;
    }

    @Override
    public History selectLastHistoryByType(String type) {
        QueryParam queryParam = new QueryParam()
                .where("type", type)
                .doPaging(0, 1)
                .orderBy("createDate").desc();
        List<History> history = historyMapper.select(queryParam);
        if (history.size() == 1) return history.get(0);
        return null;
    }
}
