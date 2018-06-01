package org.hswebframework.web.service.authorization;

import org.hswebframework.web.entity.authorization.UserMenuEntity;

import java.util.List;

/**
 * @author zhouhao
 */
public interface UserMenuManagerService {
    List<UserMenuEntity> getUserMenuAsList(String userId);

    List<UserMenuEntity> getUserMenuAsTree(String userId);

}
