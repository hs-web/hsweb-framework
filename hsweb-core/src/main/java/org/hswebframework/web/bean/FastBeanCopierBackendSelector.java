package org.hswebframework.web.bean;

import java.util.Locale;

/**
 * Selects the most suitable {@link FastBeanCopierBackend} for the current runtime.
 *
 * @author zhouhao
 * @since 5.0.2
 */
final class FastBeanCopierBackendSelector {

    static final String BACKEND_PROPERTY = "hsweb.fast-bean-copier.backend";
    static final String NATIVE_IMAGE_PROPERTY = "org.graalvm.nativeimage.imagecode";
    static final String NATIVE_HINT_PROPERTY = "hsweb.fast-bean-copier.native-image";
    static final String RUNTIME_CODEGEN_DISABLED_PROPERTY = "hsweb.fast-bean-copier.disable-codegen";

    private FastBeanCopierBackendSelector() {
    }

    static FastBeanCopierBackend selectDefaultBackend() {
        String configured = System.getProperty(BACKEND_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            return createBackend(configured);
        }
        return isRuntimeCodeGenerationDisabled()
            ? new ReflectionAccessorFastBeanCopierBackend()
            : new AsmAccessorFastBeanCopierBackend();
    }

    static boolean isRuntimeCodeGenerationDisabled() {
        String explicit = System.getProperty(RUNTIME_CODEGEN_DISABLED_PROPERTY);
        if (explicit != null) {
            return Boolean.parseBoolean(explicit);
        }
        return isNativeImageRuntime();
    }

    static boolean isNativeImageRuntime() {
        String explicit = System.getProperty(NATIVE_HINT_PROPERTY);
        if (explicit != null) {
            return Boolean.parseBoolean(explicit);
        }
        String imageCode = System.getProperty(NATIVE_IMAGE_PROPERTY);
        return imageCode != null && !imageCode.isBlank();
    }

    static FastBeanCopierBackend createBackend(String name) {
        String normalized = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "asm":
            case "asm-accessor":
                return new AsmAccessorFastBeanCopierBackend();
            case "reflection":
            case "reflection-accessor":
                return new ReflectionAccessorFastBeanCopierBackend();
            case "reflect":
                return new ReflectFastBeanCopierBackend();
            case "javassist":
                return new JavassistFastBeanCopierBackend();
            default:
                throw new IllegalArgumentException("Unsupported FastBeanCopier backend: " + name);
        }
    }
}
