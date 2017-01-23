package org.hswebframework.web.authorization;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public final class AuthorizationHolder {
    private static AuthorizationSupplier supplier;

    public Authorization get() {
        return supplier.get();
    }

    public static void setSupplier(AuthorizationSupplier supplier) {
        if (null == AuthorizationHolder.supplier)
            AuthorizationHolder.supplier = supplier;
    }
}
