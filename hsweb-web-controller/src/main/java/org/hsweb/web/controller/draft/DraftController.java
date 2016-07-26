/*
 * Copyright 2015-2016 https://github.com/hs-web/
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

package org.hsweb.web.controller.draft;

import org.hsweb.web.bean.po.draft.Draft;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.core.utils.WebUtil;
import org.hsweb.web.service.draft.DraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 草稿箱,可通过此控制器管理草稿
 */
@RestController
@RequestMapping("/draft")
@Authorize
@AccessLogger("草稿管理")
public class DraftController {

    @Resource
    private DraftService draftService;

    /**
     * 创建草稿,创建成功后返回草稿ID
     *
     * @param key   草稿标识
     * @param draft 草稿内容
     * @return 创建结果
     */
    @RequestMapping(value = "/{key}", method = RequestMethod.POST)
    @AccessLogger("创建草稿")
    public ResponseMessage createDraft(@PathVariable("key") String key,
                                       @RequestBody Draft draft) {
        User user = WebUtil.getLoginUser();
        draft.setId(Draft.createUID());
        draft.setCreateDate(new Date());
        draft.setCreatorId(user.getId());
        return ResponseMessage.ok(draftService.createDraft(key, draft));
    }

    /**
     * 获取指定标识的所有草稿信息,如果没有草稿.将返回一个空集合
     *
     * @param key 草稿标识
     * @return 草稿信息
     */
    @RequestMapping(value = "/{key}", method = RequestMethod.GET)
    @AccessLogger("获取所有草稿")
    public ResponseMessage getAllDraftByKey(@PathVariable("key") String key) {
        User user = WebUtil.getLoginUser();
        return ResponseMessage.ok(draftService.getAllDraftByKey(key, user.getId()));
    }

    /**
     * 删除一个草稿,返回是否删除成功(true or false)
     *
     * @param key 草稿标识
     * @param id  草稿ID
     * @return 删除结果
     */
    @RequestMapping(value = "/{key}/{id}", method = RequestMethod.DELETE)
    @AccessLogger("删除草稿")
    public ResponseMessage removeDraft(@PathVariable("key") String key, @PathVariable("id") String id) {
        User user = WebUtil.getLoginUser();
        return ResponseMessage.ok(draftService.removeDraft(key, id, user.getId()));
    }


    /**
     * 删除指定标识的所有草稿,返回是否删除成功(true or false)
     *
     * @param key 草稿标识
     * @return 删除结果
     */
    @RequestMapping(value = "/{key}", method = RequestMethod.DELETE)
    @AccessLogger("删除所有草稿")
    public ResponseMessage removeAllDraft(@PathVariable("key") String key) {
        User user = WebUtil.getLoginUser();
        return ResponseMessage.ok(draftService.removeDraft(key, user.getId()));
    }

}
