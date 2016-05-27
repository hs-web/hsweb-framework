package org.hsweb.web.core.session;

import org.hsweb.web.bean.po.user.User;

import javax.servlet.http.HttpSession;
import java.util.Set;

public interface HttpSessionManager {

    /**
     * 根据登陆用户的ID 获取SessionId
     *
     * @param userId 登陆用户id
     * @return session ID
     */
    String getSessionIdByUserId(String userId) ;

    /**
     * 根据用户ID从session中删除一个用户(下线)
     *
     * @param userId 要删除的用户ID
     */
    void removeUser(String userId) ;

    /**
     * 根据sessionId删除Session
     *
     * @param sessionId 要删除的sessionID
     */
    void removeSession(String sessionId) ;

    /**
     * 添加一个用户
     *
     * @param userId  用户ID
     * @param session HttpSession
     */
    void addUser(String userId, HttpSession session) ;

    Set<User> tryGetAllUser();

    /**
     * 获取当前登录的所有用户ID集合
     *
     * @return 当前登录用户ID
     */
    Set<String> getUserIdList() ;

    /**
     * 获取当前登录用户数量
     *
     * @return 登陆用户数量
     */
    int getUserTotal() ;

    /**
     * 获取所有sessionId集合
     *
     * @return sessionId集合
     */
    Set<String> getSessionIdList() ;

    /**
     * 根据用户ID 判断用户是否已经登陆
     *
     * @param userId 用户ID
     */
    boolean isLogin(String userId);
}
