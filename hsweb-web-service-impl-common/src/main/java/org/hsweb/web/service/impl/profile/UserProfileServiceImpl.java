package org.hsweb.web.service.impl.profile;

import org.hsweb.commons.MD5;
import org.hsweb.web.bean.common.UpdateParam;
import org.hsweb.web.bean.po.profile.UserProfile;
import org.hsweb.web.dao.profile.UserProfileMapper;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.hsweb.web.service.profile.UserProfileService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by zhouhao on 16-7-4.
 */
@Service("userProfileService")
public class UserProfileServiceImpl extends AbstractServiceImpl<UserProfile, String> implements UserProfileService {

    @Resource
    private UserProfileMapper userProfileMapper;

    @Override
    protected UserProfileMapper getMapper() {
        return userProfileMapper;
    }

    @Override
    public int saveOrUpdate(UserProfile userProfile) {
        UserProfile old = selectByUserIdAndType(userProfile.getUserId(), userProfile.getType());
        if (null != old) {
            return getMapper().update(UpdateParam.build(userProfile)
                    .includes("content")
                    .where("userId", userProfile.getUserId())
                    .and("type", userProfile.getType()));
        } else {
            insert(userProfile);
        }
        return 1;
    }

}
