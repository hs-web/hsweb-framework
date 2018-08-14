package org.hswebframework.web.organizational.authorization;

import org.hswebframework.web.service.authorization.AuthorizationSettingTypeSupplier;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PersonnelAuthenticationSettingTypeSupplier implements AuthorizationSettingTypeSupplier {
    public static final String SETTING_TYPE_PERSON = "person";

    public static final String SETTING_TYPE_DEPARTMENT = "department";

    public static final String SETTING_TYPE_ORG = "org";

    public static final String SETTING_TYPE_DISTRICT = "district";

    public static final String SETTING_TYPE_POSITION = "position";

    @Autowired
    private PersonnelAuthenticationManager personnelAuthenticationManager;

    @Override
    public Set<SettingInfo> get(String userId) {
        //支持职位和人员 设置权限
        PersonnelAuthentication authorization = personnelAuthenticationManager.getPersonnelAuthorizationByUserId(userId);
        if (authorization == null) {
            return new HashSet<>();
        }
        Set<SettingInfo> settingInfo = new HashSet<>();


        //人员
        SettingInfo personSetting = new SettingInfo(SETTING_TYPE_PERSON, authorization.getPersonnel().getId());
        settingInfo.add(personSetting);

        //岗位
        settingInfo.addAll(authorization.getAllPositionId()
                .stream()
                .map(id -> new SettingInfo(SETTING_TYPE_POSITION, id))
                .collect(Collectors.toSet()));

        //部门
        settingInfo.addAll(authorization.getAllDepartmentId()
                .stream()
                .map(id -> new SettingInfo(SETTING_TYPE_DEPARTMENT, id))
                .collect(Collectors.toSet()));

        //机构
        settingInfo.addAll(authorization.getAllOrgId()
                .stream()
                .map(id -> new SettingInfo(SETTING_TYPE_ORG, id))
                .collect(Collectors.toSet()));

        //行政区划
        settingInfo.addAll(authorization.getAllDistrictId()
                .stream()
                .map(id -> new SettingInfo(SETTING_TYPE_DISTRICT, id))
                .collect(Collectors.toSet()));

        return settingInfo;
    }
}
