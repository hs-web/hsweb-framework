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

import org.apache.commons.codec.binary.Base64;
import org.hswebframework.expands.security.Encrypt;
import org.hswebframework.expands.security.rsa.RSAEncrypt;
import org.hswebframework.expands.security.rsa.RSAPrivateEncrypt;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.Authorization;
import org.hswebframework.web.authorization.listener.UserAuthorizationConfigRegister;
import org.hswebframework.web.authorization.listener.UserAuthorizationListener;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.AbstractService;
import org.hswebframework.web.service.authorization.UserService;
import org.hswebframework.web.service.authorization.VerifyCode;
import org.hswebframework.web.service.authorization.VerifyCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.hswebframework.web.controller.message.ResponseMessage.ok;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@RestController
@RequestMapping("${hsweb.web.mappings.authorize:authorize}")
@AccessLogger("授权")
public class AuthorizationController {

    private static final String RSA_PRIVATE_KEY_NAME  = "RSA_PRIVATE_KEY";
    private static final String VERIFY_CODE_NAME      = "VERIFY_CODE";
    private static final String NEED_VERIFY_CODE_NAME = "NEED_VERIFY_CODE";

    @Autowired(required = false)
    private VerifyCodeGenerator verifyCodeGenerator;

    @Autowired
    private UserService userService;

    @Autowired(required = false)
    private List<UserAuthorizationListener> userAuthorizationListeners;

    @Value("${hsweb.web.authorize.rsa:false}")
    private boolean useRsa = false;

    private UserAuthorizationListenerAdapter listenerAdapter = new UserAuthorizationListenerAdapter();

    @GetMapping(value = "/public-key")
    @AccessLogger("获取公钥")
    public ResponseMessage getAuthorizeToken(HttpSession session) {
        RSAEncrypt rsaEncrypt = Encrypt.rsa();
        String publicKey = rsaEncrypt.publicEncrypt().getKey();
        String privateKey = rsaEncrypt.privateEncrypt().getKey();
        session.setAttribute(RSA_PRIVATE_KEY_NAME, privateKey);
        return ok(publicKey);
    }

    @GetMapping(value = "/verify-code")
    @AccessLogger("获取验证码")
    public void getVerifyCode(HttpServletResponse response, HttpSession session) throws IOException {
        if (verifyCodeGenerator == null) throw new NotFoundException("{verify_code_not_found}");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-disposition", "attachment;filename=verify-code.png");
        VerifyCode verifyCode = verifyCodeGenerator.generate();
        session.setAttribute(RSA_PRIVATE_KEY_NAME, verifyCode.getCode());
        verifyCode.write(response.getOutputStream());
    }

    @RequestMapping("/login-out")
    @AccessLogger("退出登录")
    public ResponseMessage loginOut(Authorization authorization) {
        listenerAdapter.onLoginOut(authorization);
        return ok();
    }

    @PostMapping(value = "/login")
    @AccessLogger("授权")
    public ResponseMessage authorize(@RequestParam String username,
                                     @RequestParam String password,
                                     String verifyCode,
                                     @RequestParam(defaultValue = "false") boolean remember,
                                     HttpSession session) {
        try {
            if (useRsa) {
                String privateKey = (String) session.getAttribute(RSA_PRIVATE_KEY_NAME);
                if (privateKey == null) throw new BusinessException("{private_key_is_null}");
                // 解密用户名密码
                try {
                    RSAEncrypt rsaEncrypt = Encrypt.rsa();
                    RSAPrivateEncrypt rsaPrivateEncrypt = rsaEncrypt.privateEncrypt(privateKey);
                    byte[] username_data = Base64.decodeBase64(username);
                    byte[] password_data = Base64.decodeBase64(password);
                    username = new String(rsaPrivateEncrypt.decrypt(username_data));
                    password = new String(rsaPrivateEncrypt.decrypt(password_data));
                } catch (Exception e) {
                    throw new BusinessException("{decrypt_param_error}", e, 400);
                }
            }
            UserAuthorizationConfigRegister configHolder = (useVerify) -> session.setAttribute(NEED_VERIFY_CODE_NAME, useVerify);
            listenerAdapter.onConfig(username, configHolder);
            Object useVerifyCode = session.getAttribute(NEED_VERIFY_CODE_NAME);
            // 尝试使用验证码验证
            if (useVerifyCode instanceof Boolean && (Boolean) useVerifyCode) {
                String realVerifyCode = (String) session.getAttribute(VERIFY_CODE_NAME);
                if (realVerifyCode == null || !realVerifyCode.equalsIgnoreCase(verifyCode)) {
                    throw new BusinessException("{verify_code_error}");
                }
            }
            listenerAdapter.onAuthorizeBefore(username);
            UserEntity entity = userService.selectByUsername(username);
            AbstractService.assertNotNull(entity, "{user_not_exists}");
            if (!entity.isEnabled()) {
                throw new BusinessException("{user_is_disabled}", 400);
            }
            password = userService.encodePassword(password, entity.getSalt());
            if (!entity.getPassword().equals(password)) {
                listenerAdapter.onAuthorizeFail(username);
                throw new BusinessException("{password_error}", 400);
            }
            // TODO: 17-1-13  获取IP
            userService.updateLoginInfo(entity.getId(), "", System.currentTimeMillis());
            // 验证通过
            Authorization authorization = userService.initUserAuthorization(entity.getId());
            listenerAdapter.onAuthorizeSuccess(remember, authorization);
            return ok(authorization.getPermissions());
        } finally {
            //无论如何都清空验证码和私钥
            session.removeAttribute(VERIFY_CODE_NAME);
            session.removeAttribute(RSA_PRIVATE_KEY_NAME);
        }
    }

    class UserAuthorizationListenerAdapter implements UserAuthorizationListener {
        @Override
        public void onConfig(String username, UserAuthorizationConfigRegister configHolder) {
            if (userAuthorizationListeners != null)
                userAuthorizationListeners.forEach(listener -> listener.onConfig(username, configHolder));
        }

        @Override
        public void onAuthorizeBefore(String username) {
            if (userAuthorizationListeners != null)
                userAuthorizationListeners.forEach(listener -> listener.onAuthorizeBefore(username));
        }

        @Override
        public void onAuthorizeFail(String username) {
            if (userAuthorizationListeners != null)
                userAuthorizationListeners.forEach(listener -> listener.onAuthorizeFail(username));
        }

        @Override
        public void onLoginOut(Authorization authorization) {
            if (userAuthorizationListeners != null)
                userAuthorizationListeners.forEach(listener -> listener.onLoginOut(authorization));
        }

        @Override
        public void onAuthorizeSuccess(boolean isRemembered, Authorization authorization) {
            if (userAuthorizationListeners != null)
                userAuthorizationListeners.forEach(listener -> listener.onAuthorizeSuccess(isRemembered, authorization));
        }
    }

}
