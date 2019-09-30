package org.hswebframework.web.authorization.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.authorization.*;
import org.hswebframework.web.service.QueryService;
import org.hswebframework.web.service.authorization.AuthorizationSettingService;
import org.hswebframework.web.service.authorization.RoleService;
import org.hswebframework.web.service.authorization.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/autz-setting/detail")
public class AuthorizationPermissionSettingDetailController {

    @Autowired
    private AuthorizationSettingService settingService;

    @Autowired
    private ApplicationContext context;

    @AllArgsConstructor
    enum SettingFor {
        user(UserService.class, t -> ((UserEntity) t).getName()),

        role(RoleService.class, t -> ((RoleEntity) t).getName());

        Class<? extends QueryService<? extends GenericEntity<String>, String>> serviceType;

        Function<GenericEntity<String>, String> nameMapper;

        Map<String, String> findMapping(ApplicationContext context, List<String> idList) {
            QueryService<? extends GenericEntity<String>, String> queryService = context.getBean(serviceType);
            return queryService.selectByPk(idList)
                    .stream()
                    .collect(Collectors.toMap(GenericEntity::getId, nameMapper, (_1, _2) -> _1));
        }

    }


    @GetMapping("/{permissionId}")
    @Authorize(permission = "autz-setting", action = Permission.ACTION_GET)
    public ResponseMessage<Map<String, List<SettingInfo>>> getSettingInfoByPermissionId(@PathVariable String permissionId) {

        List<AuthorizationSettingEntity> entities = settingService.selectByPermissionId(permissionId);

        Map<String, Map<String, String>> nameMapping = entities.stream()
                .collect(Collectors.groupingBy(AuthorizationSettingEntity::getType,
                        Collectors.collectingAndThen(Collectors.toList(),
                                list -> SettingFor.valueOf(list.get(0).getType()).findMapping(context, list.stream()
                                        .map(AuthorizationSettingEntity::getSettingFor)
                                        .collect(Collectors.toList())))));

        return ResponseMessage.ok(

                entities.stream()
                        .map(entity -> {
                            SettingInfo info = SettingInfo.of(entity);
                            Optional.ofNullable(nameMapping.get(entity.getType()))
                                    .map(map -> map.get(entity.getSettingFor()))
                                    .ifPresent(info::setSettingForName);
                            return info;
                        }).collect(Collectors.groupingBy(SettingInfo::getType))
        );
    }


    @Getter
    @Setter
    public static class SettingInfo extends SimpleAuthorizationSettingEntity {

        private String settingForName;

        private AuthorizationSettingDetailEntity detail;

        private static SettingInfo of(AuthorizationSettingEntity entity) {
            SettingInfo info= entity.copyTo(new SettingInfo());
            if(!CollectionUtils.isEmpty(info.getDetails())){
                info.setDetail(info.getDetails().get(0));
                info.setDetails(null);
            }
            return info;
        }
    }
}
