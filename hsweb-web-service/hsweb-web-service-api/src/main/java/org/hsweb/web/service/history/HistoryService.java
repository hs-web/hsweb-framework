package org.hsweb.web.service.history;

import org.hsweb.web.bean.po.history.History;
import org.hsweb.web.service.GenericService;

/**
 * Created by zhouhao on 16-4-22.
 */
public interface HistoryService extends GenericService<History, String> {
    /**
     * 根据类型，查询最后一条历史记录，如果不存在返回null
     *
     * @param type 类型
     * @return 查询结果
     */
    History selectLastHistoryByType(String type);
}
