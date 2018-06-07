package org.hswebframework.web.service.authorization;

import org.hswebframework.web.entity.authorization.AuthorizationSettingMenuEntity;
import org.hswebframework.web.service.CrudService;
import org.hswebframework.web.service.TreeService;

import java.util.List;

/**
 * 权限菜单设置
 *
 * @author zhouhao
 * @see AuthorizationSettingService
 */
public interface AuthorizationSettingMenuService extends
        CrudService<AuthorizationSettingMenuEntity, String>
        , TreeService<AuthorizationSettingMenuEntity, String> {

    /**
     * 根据设置id删除菜单配置
     *
     * @param settingId 设置id  {@link org.hswebframework.web.entity.authorization.AuthorizationSettingEntity#id}
     * @return 删除的数量
     */
    int deleteBySettingId(String settingId);

    /**
     * 获取设置id对应的所有权限菜单配置
     *
     * @param settingId 设置id {@link org.hswebframework.web.entity.authorization.AuthorizationSettingEntity#id}
     * @return 永远不为nul .权限菜单设置,如果没有则返回空集合
     */
    List<AuthorizationSettingMenuEntity> selectBySettingId(String settingId);

    /**
     * 获取多个设置id对应的所有权限菜单配置
     *
     * @param settingId 设置id {@link org.hswebframework.web.entity.authorization.AuthorizationSettingEntity#id}
     * @return 永远不为nul .权限菜单设置,如果没有则返回空集合
     */
    List<AuthorizationSettingMenuEntity> selectBySettingId(List<String> settingId);
}
