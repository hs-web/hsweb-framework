package org.hsweb.web.service.impl.profile;

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


}
