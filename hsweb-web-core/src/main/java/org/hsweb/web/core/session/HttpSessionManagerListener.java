package org.hsweb.web.core.session;

import org.hsweb.web.bean.po.user.User;

import javax.servlet.http.HttpSession;

/**
 * Created by zhouhao on 16-6-2.
 */
public interface HttpSessionManagerListener {
    void onUserLogin(User user,HttpSession session);

    void onUserLoginOut(String userId,HttpSession session);
}
