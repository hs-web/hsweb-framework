package org.hswebframework.web.controller.authorization;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.entity.authorization.UserSettingEntity;
import org.hswebframework.web.service.authorization.UserSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<UserSettingEntity> get(Authentication authentication,
                                                 @PathVariable String key,
                                                 @PathVariable String id) {
        return ResponseEntity.ok(userSettingService.selectByUser(authentication.getUser().getId(), key, id));
    }

    @GetMapping("/me/{key}")
    @Authorize(merge = false)
    @ApiOperation("获取当前用户的配置列表")
    public ResponseEntity<List<UserSettingEntity>> get(Authentication authentication,
                                                       @PathVariable String key) {
        return ResponseEntity.ok(userSettingService.selectByUser(authentication.getUser().getId(), key));
    }

    @PatchMapping("/me/{key}")
    @Authorize(merge = false)
    @ApiOperation("获取当前用户的配置列表")
    public ResponseEntity<String> save(Authentication authentication,
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
        return ResponseEntity.ok(id);
    }
}
