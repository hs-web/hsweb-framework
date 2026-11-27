package org.hswebframework.web.exception;

import lombok.Getter;

/**
 * 未知远端异常的安全包装，不加载远端类或解析堆栈。
 * 已知平台异常使用对应的 NoStackTrace 实现，并携带相同的远端描述。
 */
@Getter
public class RemoteException extends BusinessException.NoStackTrace implements RemoteExceptionMetadata {
    private static final long serialVersionUID = 1L;

    private final Description remoteDescription;

    public RemoteException(Description description) {
        super(description.i18nCode(), description.code(),
              description.status() == null ? 500 : description.status(), description.arguments());
        remoteDescription = description;
    }

    @Override
    public String getOriginalMessage() {
        return remoteDescription.message();
    }

    @Getter
    public static final class I18n extends I18nSupportException.NoStackTrace implements RemoteExceptionMetadata {
        private final Description remoteDescription;

        public I18n(Description description) {
            super(description.i18nCode(), description.arguments());
            remoteDescription = description;
        }

        @Override
        public String getOriginalMessage() {
            return remoteDescription.message();
        }
    }

    @Getter
    public static final class NotFound extends NotFoundException.NoStackTrace implements RemoteExceptionMetadata {
        private final Description remoteDescription;

        public NotFound(Description description) {
            super(description.i18nCode(), description.arguments());
            remoteDescription = description;
        }

        @Override
        public String getOriginalMessage() {
            return remoteDescription.message();
        }
    }

    @Getter
    public static final class Validation extends ValidationException.NoStackTrace implements RemoteExceptionMetadata {
        private final Description remoteDescription;

        public Validation(Description description) {
            super(description.i18nCode(), description.details(), description.arguments());
            remoteDescription = description;
        }

        @Override
        public String getOriginalMessage() {
            return remoteDescription.message();
        }
    }
}
