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

package org.hsweb.web.controller.plan;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.plan.QueryPlan;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.core.utils.WebUtil;
import org.hsweb.web.service.plan.QueryPlanService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 查询方案控制器
 * Created by hsweb-generator
 */
@RestController
@RequestMapping(value = "/query-plan")
@AccessLogger("查询方案")
@Authorize(module = "query-plan")
public class QueryPlanController extends GenericController<QueryPlan, String> {

    @Resource
    private QueryPlanService queryPlanService;

    @Override
    public QueryPlanService getService() {
        return this.queryPlanService;
    }

    @Override
    @Authorize(action = "admin")
    public ResponseMessage list(QueryParam param) {
        return super.list(param);
    }

    @RequestMapping(value = "/type/{type}", method = RequestMethod.GET)
    @AccessLogger("当前用户对应的类型")
    public ResponseMessage byLoginUserAndType(@PathVariable("type") String type) {
        User user = WebUtil.getLoginUser();
        // where type=#{type} and (create_id=#{user.id} or sharing=1)
        QueryParam param = QueryParam.build().noPaging();
        param.where("type", type)
                .nest("creatorId", user.getId()).or("sharing", 1);
        return ResponseMessage.ok(queryPlanService.select(param)).onlyData();
    }

    @Override
    public ResponseMessage add(@RequestBody QueryPlan object) {
        User user = WebUtil.getLoginUser();
        object.setCreateDate(new Date());
        object.setCreatorId(user.getId());
        return super.add(object);
    }

    @Override
    public ResponseMessage info(@PathVariable("id") String id) {
        QueryPlan plan = queryPlanService.selectByPk(id);
        validPlan(plan);
        return ResponseMessage.ok(plan);
    }

    @Override
    public ResponseMessage delete(@PathVariable("id") String id) {
        QueryPlan plan = queryPlanService.selectByPk(id);
        validPlan(plan);
        return ResponseMessage.ok(queryPlanService.delete(id));
    }

    @Override
    public ResponseMessage update(@PathVariable("id") String id, @RequestBody QueryPlan object) {
        QueryPlan plan = queryPlanService.selectByPk(id);
        validPlan(plan);
        object.setId(id);
        return ResponseMessage.ok(queryPlanService.update(object));
    }

    @Override
    public ResponseMessage update(@RequestBody String json) {
        throw new NotFoundException("");
    }

    void validPlan(QueryPlan plan) {
        User user = WebUtil.getLoginUser();
        //方案不存在或者方案未共享并且不是由本人创建
        if (plan == null || (!plan.isSharing() && !user.getId().equals(plan.getCreatorId())))
            throw new NotFoundException("方案不存在");
    }
}
