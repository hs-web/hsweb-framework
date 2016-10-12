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

import org.hsweb.web.bean.po.quartz.QuartzJob;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.utils.WebUtil;
import org.hsweb.web.service.impl.AbstractTestCase;
import org.hsweb.web.service.quartz.QuartzJobService;
import org.hsweb.web.service.user.UserService;
import org.junit.Test;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author zhouhao
 */
@Component
public class QuartzJobServiceImplTest extends AbstractTestCase {

    @Resource
    private QuartzJobService quartzJobService;

    @Resource
    private UserService userService;
    static final String jobId = "test";

    @Test
    public void testJob() throws InterruptedException {
        User user = new User();
        user.setName("admin");
        user.setUsername("admin");
        user.setPassword("admin");
        userService.insert(user);
        quartzJobService.delete(jobId);
        QuartzJob job = new QuartzJob();
        job.setId(jobId);
        job.setName("测试任务");
        job.setCron("0/2 * * * * ?");
        job.setLanguage("groovy");
        job.setScript("println('任务执行中...'+(org.hsweb.web.core.utils.WebUtil.getLoginUser())); return 'aaaaa';");
        quartzJobService.insert(job);
        Thread.sleep(20 * 1000);
//        job.setCron("0/5 * * * * ?");
//        job.setScript("println('任务执行中22222...');return 'aaaaa';");
//        quartzJobService.update(job);
        Thread.sleep(10 * 1000);
        quartzJobService.disable(job.getId());
        Thread.sleep(10 * 1000);
        quartzJobService.enable(job.getId());
        Thread.sleep(30 * 1000);
        quartzJobService.delete(jobId);
        Thread.sleep(5 * 1000);
    }

}