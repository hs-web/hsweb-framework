/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.service.impl.quartz;

import org.hsweb.ezorm.core.dsl.Update;
import org.hsweb.web.bean.common.InsertParam;
import org.hsweb.web.bean.common.PagerResult;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.common.UpdateParam;
import org.hsweb.web.bean.po.quartz.QuartzJobHistory;
import org.hsweb.web.bean.po.quartz.QuartzJobHistory.Property;
import org.hsweb.web.dao.quartz.QuartzJobHistoryMapper;
import org.hsweb.web.service.quartz.QuartzJobHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 定时调度任务执行记录服务类
 * Created by generator
 */
@Service("quartzJobHistoryService")
public class QuartzJobHistoryServiceImpl implements QuartzJobHistoryService {

    @Resource
    protected QuartzJobHistoryMapper quartzJobHistoryMapper;

    @Override
    public PagerResult<QuartzJobHistory> selectPager(QueryParam param) {
        PagerResult<QuartzJobHistory> result = new PagerResult<>();
        int total = total(param);
        result.setTotal(total);
        param.rePaging(total);
        result.setData(select(param));
        return result;
    }

    @Override
    public List<QuartzJobHistory> select(QueryParam param) {
        return quartzJobHistoryMapper.select(param);
    }

    @Override
    public int total(QueryParam param) {
        return quartzJobHistoryMapper.total(param);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String createAndInsertHistory(String jobId) {
        QuartzJobHistory history = new QuartzJobHistory();
        history.setId(QuartzJobHistory.createUID());
        history.setStatus(QuartzJobHistory.Status.RUNNING.getValue());
        history.setStartTime(new Date());
        history.setJobId(jobId);
        quartzJobHistoryMapper.insert(InsertParam.build(history));
        return history.getId();
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public boolean endHistory(String historyId, String result, QuartzJobHistory.Status status) {
        return Update.build(quartzJobHistoryMapper::update, new UpdateParam<>())
                .set(Property.result, result)
                .set(Property.status, status.getValue())
                .set(Property.endTime, new Date())
                .where(Property.id, historyId).exec() == 1;
    }
}
