package org.hsweb.web.controller.draft;

import org.hsweb.web.bean.po.draft.Draft;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.authorize.annotation.Authorize;
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
 * 草稿箱，基于缓存
 * Created by zhouhao on 16-6-2.
 */
@RestController
@RequestMapping("/draft")
@Authorize
public class DraftController {

    @Resource
    private DraftService draftService;

    @RequestMapping(value = "/{key}", method = RequestMethod.POST)
    public ResponseMessage createDraft(@PathVariable("key") String key,
                                       @RequestBody Draft draft) {
        User user = WebUtil.getLoginUser();
        draft.setId(Draft.createUID());
        draft.setCreateDate(new Date());
        draft.setCreatorId(user.getId());
        return ResponseMessage.ok(draftService.createDraft(key + user.getId(), draft));
    }

    @RequestMapping(value = "/{key}", method = RequestMethod.GET)
    public ResponseMessage getAllDraftByKey(@PathVariable("key") String key) {
        User user = WebUtil.getLoginUser();
        return ResponseMessage.ok(draftService.getAllDraftByKey(key, user.getId()));
    }

    @RequestMapping(value = "/{key}/{id}", method = RequestMethod.DELETE)
    public ResponseMessage removeDraft(@PathVariable("key") String key, @PathVariable("id") String id) {
        User user = WebUtil.getLoginUser();
        draftService.removeDraft(key, id, user.getId());
        return ResponseMessage.ok();
    }


    @RequestMapping(value = "/{key}", method = RequestMethod.DELETE)
    public ResponseMessage removeAllDraft(@PathVariable("key") String key) {
        User user = WebUtil.getLoginUser();
        draftService.removeDraft(key, user.getId());
        return ResponseMessage.ok();
    }

}
