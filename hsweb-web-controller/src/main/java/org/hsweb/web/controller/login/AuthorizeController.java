package org.hsweb.web.controller.login;

import org.apache.commons.beanutils.BeanUtils;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.exception.AuthorizeException;
import org.hsweb.web.core.exception.AuthorizeForbiddenException;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.core.session.HttpSessionManager;
import org.hsweb.web.core.utils.WebUtil;
import org.hsweb.web.service.config.ConfigService;
import org.hsweb.web.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.webbuilder.utils.common.MD5;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by zhouhao on 16-4-29.
 */
@RestController
public class AuthorizeController {
    @Autowired(required = false)
    private CacheManager cacheManager;

    @Resource
    private UserService userService;

    @Resource
    private ConfigService configService;

    @Autowired
    private HttpSessionManager httpSessionManager;

    @RequestMapping(value = "/online/total", method = RequestMethod.GET)
    @AccessLogger("当前在线总人数")
    @Authorize
    public ResponseMessage onlineTotal() {
        return ResponseMessage.ok(httpSessionManager.getUserTotal());
    }

    @RequestMapping(value = "/online", method = RequestMethod.GET)
    @AccessLogger("当前在线用户ID")
    @Authorize
    public ResponseMessage online() {
        return ResponseMessage.ok(httpSessionManager.getUserIdList());
    }

    @RequestMapping(value = "/online/list", method = RequestMethod.GET)
    @AccessLogger("当前在线用户")
    @Authorize
    public ResponseMessage online(QueryParam param) {
        param.includes("id", "username", "name", "phone", "email");
        param.excludes("password");
        return ResponseMessage.ok(httpSessionManager.tryGetAllUser())
                .include(User.class, param.getIncludes())
                .exclude(User.class, param.getExcludes());
    }

    @RequestMapping(value = "/exit", method = RequestMethod.POST)
    @AccessLogger("登出")
    public ResponseMessage exit() throws Exception {
        User user = WebUtil.getLoginUser();
        if (user != null) {
            httpSessionManager.removeUser(user.getId());
        }
        return ResponseMessage.ok();
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @AccessLogger("登录")
    public ResponseMessage login(@RequestParam String username, @RequestParam String password, HttpServletRequest request) throws Exception {
        //判断用户是否多次输入密码错误
        String userIp = WebUtil.getIpAddr(request);
        int maxErrorNumber = configService.getInt("login", "error.max_number", 5);
        int waitMinutes = configService.getInt("login", "error.wait_minutes", 10);
        Cache cache = cacheManager.getCache("login.error");
        String cachePrefix = username.concat("@").concat(userIp);
        String timeCacheKey = cachePrefix.concat("-time");
        String numberCacheKey = cachePrefix.concat("-number");
        Integer error_number = cache.get(numberCacheKey, Integer.class);
        Long error_time = cache.get(timeCacheKey, Long.class);
        long now_time = System.currentTimeMillis();
        if (error_number != null && error_time != null) {
            if ((now_time - error_time) / 1000 / 60d > waitMinutes) {
                cache.evict(timeCacheKey);
                cache.evict(numberCacheKey);
                error_number = 0;
                error_time = 0l;
            }
            if (error_number >= maxErrorNumber)
                throw new AuthorizeForbiddenException("您的账户已被锁定登录,请" + (waitMinutes - ((now_time - error_time) / 1000 / 60)) + "分钟后再试!");
        }
        User user = userService.selectByUserName(username);
        if (user == null || user.getStatus() != 1) throw new NotFoundException("用户不存在或已注销");
        //密码错误
        if (!user.getPassword().equals(MD5.encode(password))) {
            if (error_number == null) error_number = 0;
            cache.put(timeCacheKey, System.currentTimeMillis());
            cache.put(numberCacheKey, ++error_number);
            throw new AuthorizeForbiddenException("密码错误,你还可以重试" + (maxErrorNumber - error_number) + "次");
        }
        cache.evict(timeCacheKey);
        cache.evict(numberCacheKey);
        user.setPassword("");//去除密码
        if (user.getUsername().equals("admin"))
            userService.initAdminUser(user);
        else
            user.initRoleInfo();
        httpSessionManager.addUser((User) BeanUtils.cloneBean(user), request.getSession());
        return ResponseMessage.ok();
    }

    @PostConstruct
    public void init() {
        if (cacheManager == null) {
            cacheManager = new ConcurrentMapCacheManager();
        }
    }

}
