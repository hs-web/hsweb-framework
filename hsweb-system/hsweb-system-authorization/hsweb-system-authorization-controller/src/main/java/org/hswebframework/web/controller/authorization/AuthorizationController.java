/*
 * Copyright 2016 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web.controller.authorization;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.listener.AuthorizationListenerDispatcher;
import org.hswebframework.web.authorization.listener.event.*;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.authorization.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Function;

import static org.hswebframework.web.controller.message.ResponseMessage.ok;

/**
 * @author zhouhao
 */
@RestController
@RequestMapping("${hsweb.web.mappings.authorize:authorize}")
@AccessLogger("授权")
@Api(tags = "hsweb-authorization", description = "提供基本的授权功能")
public class AuthorizationController {

//    private static final String RSA_PRIVATE_KEY_NAME  = "RSA_PRIVATE_KEY";
//    private static final String VERIFY_CODE_NAME      = "VERIFY_CODE";

    @Autowired
    private UserService userService;

    @Autowired
    private AuthorizationListenerDispatcher authorizationListenerDispatcher;

//    @GetMapping(value = "/public-key")
//    @AccessLogger("获取公钥")
//    @ApiOperation("获取rsa公钥,当开启了用户名密码加密的时候使用此接口获取用于加密的公钥")
//    public ResponseMessage getAuthorizeToken(@ApiParam(hidden = true) HttpSession session) {
//        RSAEncrypt rsaEncrypt = Encrypt.rsa();
//        String publicKey = rsaEncrypt.publicEncrypt().getKey();
//        String privateKey = rsaEncrypt.privateEncrypt().getKey();
//        session.setAttribute(RSA_PRIVATE_KEY_NAME, privateKey);
//        return ok(publicKey);
//    }

    @GetMapping("/login-out")
    @AccessLogger("退出登录")
    @Authorize
    @ApiOperation("退出当前登录")
    public ResponseMessage exit(@ApiParam(hidden = true) Authentication authentication) {
        authorizationListenerDispatcher.doEvent(new AuthorizationExitEvent(authentication));
        return ok();
    }

    @PostMapping(value = "/login")
    @AccessLogger("授权")
    @ApiOperation("用户名密码登录")
    public ResponseMessage<String> authorize(@RequestParam @ApiParam("用户名") String username,
                                             @RequestParam @ApiParam("密码") String password,
                                             @ApiParam(hidden = true) HttpServletRequest request) {

        AuthorizationFailedEvent.Reason reason = AuthorizationFailedEvent.Reason.OTHER;
        Function<String, Object> parameterGetter = request::getParameter;
        try {
            AuthorizationDecodeEvent decodeEvent = new AuthorizationDecodeEvent(username, password, parameterGetter);
            authorizationListenerDispatcher.doEvent(decodeEvent);
            username = decodeEvent.getUsername();
            password = decodeEvent.getPassword();

            AuthorizationBeforeEvent beforeEvent = new AuthorizationBeforeEvent(username, password, parameterGetter);
            authorizationListenerDispatcher.doEvent(beforeEvent);

//            if (useRsa) {
//                String privateKey = (String) session.getAttribute(RSA_PRIVATE_KEY_NAME);
//                if (privateKey == null) throw new BusinessException("{private_key_is_null}");
//                // 解密用户名密码
//                try {
//                    RSAEncrypt rsaEncrypt = Encrypt.rsa();
//                    RSAPrivateEncrypt rsaPrivateEncrypt = rsaEncrypt.privateEncrypt(privateKey);
//                    byte[] username_data = Base64.decodeBase64(username);
//                    byte[] password_data = Base64.decodeBase64(password);
//                    username = new String(rsaPrivateEncrypt.decrypt(username_data));
//                    password = new String(rsaPrivateEncrypt.decrypt(password_data));
//                } catch (Exception e) {
//                    throw new BusinessException("{decrypt_param_error}", e, 400);
//                }
//            }

//            UserAuthorizationConfigRegister configHolder = (useVerify) -> session.setAttribute(NEED_VERIFY_CODE_NAME, useVerify);
//            listenerAdapter.onConfig(username, configHolder);
//            Object useVerifyCode = session.getAttribute(NEED_VERIFY_CODE_NAME);
//            // 尝试使用验证码验证
//            if (Boolean.TRUE.equals(useVerifyCode)) {
//                String realVerifyCode = (String) session.getAttribute(VERIFY_CODE_NAME);
//                if (realVerifyCode == null || !realVerifyCode.equalsIgnoreCase(verifyCode)) {
//                    throw new BusinessException("{verify_code_error}");
//                }
//            }
//            listenerAdapter.onAuthorizeBefore(username);
            UserEntity entity = userService.selectByUsername(username);
            if (entity == null) {
                reason = AuthorizationFailedEvent.Reason.USER_NOT_EXISTS;
                throw new NotFoundException("{user_not_exists}");
            }
            if (Boolean.FALSE.equals(entity.isEnabled())) {
                reason = AuthorizationFailedEvent.Reason.USER_DISABLED;
                throw new BusinessException("{user_is_disabled}", 400);
            }
            password = userService.encodePassword(password, entity.getSalt());
            if (!entity.getPassword().equals(password)) {
                reason = AuthorizationFailedEvent.Reason.PASSWORD_ERROR;
                throw new BusinessException("{password_error}", 400);
            }
            // TODO: 17-1-13  获取IP
            userService.updateLoginInfo(entity.getId(), "", System.currentTimeMillis());
            // 验证通过
            Authentication authentication = userService.initUserAuthorization(entity.getId());
            AuthorizationSuccessEvent event = new AuthorizationSuccessEvent(authentication, parameterGetter);
            authorizationListenerDispatcher.doEvent(event);
            return ok(entity.getId());
        } catch (Exception e) {
            AuthorizationFailedEvent failedEvent = new AuthorizationFailedEvent(username, password, parameterGetter, reason);
            failedEvent.setException(e);
            authorizationListenerDispatcher.doEvent(failedEvent);
            throw e;
        } finally {
            //无论如何都清空验证码和私钥
//            session.removeAttribute(VERIFY_CODE_NAME);
//            session.removeAttribute(RSA_PRIVATE_KEY_NAME);
        }
    }

}
