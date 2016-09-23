package org.hsweb.web.core.session.simple;

import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.session.HttpSessionManager;
import org.hsweb.web.core.utils.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class UserLoginOutListener implements HttpSessionListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private HttpSessionManager httpSessionManager;

    /* Session创建事件 */
    public void sessionCreated(HttpSessionEvent se) {
        logger.info("session created:" + se.getSession().getId());
    }

    /* Session失效事件 */
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        try {
            User user = WebUtil.getLoginUser(session);
            if (user != null) {
                httpSessionManager.removeUser(user.getId());
            }
        } catch (Exception e) {
            logger.error("remove session or user error!", e);
        }
    }

    public void setHttpSessionManager(HttpSessionManager httpSessionManager) {
        this.httpSessionManager = httpSessionManager;
    }
}