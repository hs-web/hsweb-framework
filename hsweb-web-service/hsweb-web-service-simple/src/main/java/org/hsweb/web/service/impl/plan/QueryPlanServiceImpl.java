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

package org.hsweb.web.service.impl.plan;

import org.hsweb.web.bean.po.plan.QueryPlan;
import org.hsweb.web.dao.plan.QueryPlanMapper;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.hsweb.web.service.plan.QueryPlanService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static org.hsweb.web.bean.po.plan.QueryPlan.Property.*;

/**
 * 查询方案服务类
 * Created by generator
 */
@Service("queryPlanService")
public class QueryPlanServiceImpl extends AbstractServiceImpl<QueryPlan, String> implements QueryPlanService {

    @Resource
    protected QueryPlanMapper queryPlanMapper;

    @Override
    protected QueryPlanMapper getMapper() {
        return this.queryPlanMapper;
    }

    @Override
    public int update(QueryPlan data) {
        return createUpdate(data).includes(name, config, sharing).where(id, data.getId()).exec();
    }
}
