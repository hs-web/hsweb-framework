package org.hswebframework.web.service.authorization;

import org.hswebframework.web.entity.authorization.UserMenuEntity;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface UserMenuManagerService {
    List<UserMenuEntity> getUserMenuAsList(String userId);

    List<UserMenuEntity> getUserMenuAsTree(String userId);

}
