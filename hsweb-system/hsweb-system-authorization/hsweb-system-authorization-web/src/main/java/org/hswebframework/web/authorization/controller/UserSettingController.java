package org.hswebframework.web.authorization.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.authorization.UserSettingEntity;
import org.hswebframework.web.service.authorization.UserSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zhouhao
 * @since 3.0
 */
@RestController
@RequestMapping("/user-setting")
@Authorize//(permission = "user-setting", description = "用户配置管理")
@Api(value = "用户配置管理", tags = "用户-用户配置管理")
public class UserSettingController {

    @Autowired
    private UserSettingService userSettingService;

    @GetMapping("/me/{key}/{id}")
    @Authorize(merge = false)
    @ApiOperation("获取当前用户的配置")
    public ResponseMessage<UserSettingEntity> get(Authentication authentication,
                                                  @PathVariable String key,
                                                  @PathVariable String id) {
        return ResponseMessage.ok(userSettingService.selectByUser(authentication.getUser().getId(), key, id));
    }

    @GetMapping("/me/{key}")
    @Authorize(merge = false)
    @ApiOperation("获取当前用户的配置列表")
    public ResponseMessage<List<UserSettingEntity>> get(Authentication authentication,
                                                       @PathVariable String key) {
        return ResponseMessage.ok(userSettingService.selectByUser(authentication.getUser().getId(), key));
    }

    @PatchMapping("/me/{key}")
    @Authorize(merge = false)
    @ApiOperation("获取当前用户的配置列表")
    public ResponseMessage<String> save(Authentication authentication,
                                       @PathVariable String key,
                                       @Validated
                                       @RequestBody UserSettingEntity userSettingEntity) {
        userSettingEntity.setId(null);
        userSettingEntity.setUserId(authentication.getUser().getId());
        userSettingEntity.setKey(key);
        UserSettingEntity old = userSettingService.selectByUser(authentication.getUser().getId(), key, userSettingEntity.getSettingId());
        if (old != null) {
            userSettingEntity.setId(old.getId());
        }
        String id = userSettingService.saveOrUpdate(userSettingEntity);
        return ResponseMessage.ok(id);
    }
}
