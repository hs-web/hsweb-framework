package org.hswebframework.web.exception;

import org.hswebframework.web.i18n.LocaleUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

/**
 * 恢复后的异常携带的远端描述。再次转 Map 时保留远端类型和诊断信息。
 */
public interface RemoteExceptionMetadata {

    Description getRemoteDescription();

    default String getRemoteType() {
        return getRemoteDescription().type();
    }

    default String getRemoteDetail() {
        return getRemoteDescription().detail();
    }

    default Integer getRemoteStatus() {
        return getRemoteDescription().status();
    }

    /**
     * 发送端 message 已是展示文本，不能作为 MessageFormat 模板再次格式化。
     */
    default String resolveRemoteMessage(Locale locale, Object[] args) {
        Description description = getRemoteDescription();
        String translated = StringUtils.hasText(description.i18nCode())
            ? LocaleUtils.resolveMessage(description.i18nCode(), locale, null, args) : null;
        return translated == null ? description.message() : translated;
    }

    record Description(String type,
                       String message,
                       String detail,
                       String i18nCode,
                       Object[] args,
                       String code,
                       Integer status,
                       List<ValidationException.Detail> details) implements Serializable {
        public Description {
            Assert.hasText(type, "Remote exception type is required");
        }

        public Object[] arguments() {
            return args == null ? new Object[0] : args;
        }
    }
}
