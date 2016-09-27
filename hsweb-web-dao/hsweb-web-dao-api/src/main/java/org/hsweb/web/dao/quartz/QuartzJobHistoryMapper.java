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

package org.hsweb.web.dao.quartz;

import org.hsweb.web.bean.po.quartz.QuartzJobHistory;
import org.hsweb.web.dao.GenericMapper;

/**
* 定时调度任务执行记录数据映射接口
* Created by generator 
*/
public interface QuartzJobHistoryMapper extends GenericMapper<QuartzJobHistory,String> {

}
