package org.hswebframework.web.service.authorization;

import org.hswebframework.web.entity.authorization.AuthorizationSettingMenuEntity;
import org.hswebframework.web.service.CrudService;
import org.hswebframework.web.service.TreeService;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface AuthorizationSettingMenuService extends
        CrudService<AuthorizationSettingMenuEntity, String>
        , TreeService<AuthorizationSettingMenuEntity, String> {

    int deleteBySettingId(String settingId);

    List<AuthorizationSettingMenuEntity> selectBySettingId(String settingId);

    List<AuthorizationSettingMenuEntity> selectBySettingId(List<String> settingId);
}
