package org.hsweb.web.service.impl.history;

import org.hsweb.web.bean.po.history.History;
import org.hsweb.web.dao.history.HistoryMapper;
import org.hsweb.web.service.history.HistoryService;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static org.hsweb.web.bean.po.history.History.Property;

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
        return createQuery()
                .where(Property.type, type)
                .orderByDesc(Property.createDate)
                .single();
    }
}
