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

package org.hsweb.web.service.quartz;

import org.hsweb.web.bean.po.quartz.QuartzJob;
import org.hsweb.web.service.GenericService;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 定时调度任务服务类
 * Created by generator
 */
public interface QuartzJobService extends GenericService<QuartzJob, String> {

    void enable(String id);

    void disable(String id);

    List<Date> getExecTimes(String cron, int number);

    Object execute(String id, Map<String,Object> var);
}
