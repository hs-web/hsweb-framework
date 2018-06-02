package org.hswebframework.web.service.authorization;

import org.hswebframework.web.entity.authorization.UserMenuEntity;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * 用户菜单管理服务,用户获取用户分配的菜单信息
 *
 * @author zhouhao
 * @see AuthorizationSettingService
 * @since 3.0
 */
public interface UserMenuManagerService {
    /**
     * 获取用户菜单,返回平铺的list结果,{@link  UserMenuEntity#getChildren()} 永远返回null
     *
     * @param userId 用户ID,不能为空
     * @return 永远不为<code>null</code>,用户不存在或者没有任何菜单时,返回空集合
     */
    List<UserMenuEntity> getUserMenuAsList(String userId);

    /**
     * 获取用户菜单,返回树形结构的根节点,通过{@link  UserMenuEntity#getChildren()} 获取子节点
     *
     * @param userId 用户ID,不能为空
     * @return 永远不为<code>null</code>,用户不存在或者没有任何菜单时,返回空集合
     * @see org.hswebframework.web.commons.entity.TreeSupportEntity#list2tree(Collection, BiConsumer)
     * @see UserMenuEntity#getChildren()
     */
    List<UserMenuEntity> getUserMenuAsTree(String userId);

}
