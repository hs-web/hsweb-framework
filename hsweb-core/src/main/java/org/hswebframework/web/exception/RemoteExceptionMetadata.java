package org.hswebframework.web.exception;

import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.List;

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
