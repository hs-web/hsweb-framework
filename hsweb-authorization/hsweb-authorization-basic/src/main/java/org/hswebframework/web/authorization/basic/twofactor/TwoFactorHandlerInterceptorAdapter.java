package org.hswebframework.web.authorization.basic.twofactor;

import lombok.AllArgsConstructor;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.authorization.annotation.TwoFactor;
import org.hswebframework.web.authorization.exception.NeedTwoFactorException;
import org.hswebframework.web.authorization.twofactor.TwoFactorValidator;
import org.hswebframework.web.authorization.twofactor.TwoFactorValidatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhouhao
 * @since 3.0.4
 */
@AllArgsConstructor
public class TwoFactorHandlerInterceptorAdapter extends HandlerInterceptorAdapter {

    private TwoFactorValidatorManager validatorManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = ((HandlerMethod) handler);
            TwoFactor factor = method.getMethodAnnotation(TwoFactor.class);
            if (factor == null || factor.ignore()) {
                return true;
            }
            String userId = Authentication.current()
                    .map(Authentication::getUser)
                    .map(User::getId)
                    .orElse(null);
            TwoFactorValidator validator = validatorManager.getValidator(userId, factor.value(), factor.provider());
            if (!validator.expired()) {
                return true;
            }
            String code = request.getParameter(factor.parameter());
            if (code == null) {
                code = request.getHeader(factor.parameter());
            }
            if (StringUtils.isEmpty(code)) {
                throw new NeedTwoFactorException("需要进行双重验证", factor.provider());
            } else if (!validator.verify(code, factor.timeout())) {
                throw new NeedTwoFactorException("验证码错误", factor.provider());
            }
        }
        return super.preHandle(request, response, handler);
    }
}
